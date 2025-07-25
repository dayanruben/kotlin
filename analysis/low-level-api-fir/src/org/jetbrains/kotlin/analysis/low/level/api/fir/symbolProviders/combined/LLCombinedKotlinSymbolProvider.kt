/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.symbolProviders.combined

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analysis.api.platform.declarations.KotlinDeclarationProvider
import org.jetbrains.kotlin.analysis.api.platform.declarations.mergeDeclarationProviders
import org.jetbrains.kotlin.analysis.api.platform.packages.KotlinPackageProvider
import org.jetbrains.kotlin.analysis.api.platform.packages.mergePackageProviders
import org.jetbrains.kotlin.analysis.api.platform.caches.NullableCaffeineCache
import org.jetbrains.kotlin.analysis.api.platform.caches.withStatsCounter
import org.jetbrains.kotlin.analysis.low.level.api.fir.symbolProviders.LLKotlinSymbolProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.statistics.LLStatisticsService
import org.jetbrains.kotlin.analysis.low.level.api.fir.symbolProviders.LLModuleSpecificSymbolProviderAccess
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.resolve.providers.FirCompositeCachedSymbolNamesProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolNamesProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProviderInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtCallableDeclaration

/**
 * [LLCombinedKotlinSymbolProvider] combines multiple [LLKotlinSymbolProvider]s with the following advantages:
 *
 * - The combined symbol provider can combine the "names in package" sets built by individual providers. The name set can then be checked
 *   once instead of for each subordinate symbol provider. Because Kotlin symbol providers are ordered first in
 *   [LLDependenciesSymbolProvider][org.jetbrains.kotlin.analysis.low.level.api.fir.symbolProviders.LLDependenciesSymbolProvider],
 *   this check is especially fruitful.
 * - For a given class or callable ID, indices can be accessed once to get relevant PSI elements. Then the correct symbol provider(s) to
 *   call can be found out via the PSI element's [KaModule][org.jetbrains.kotlin.analysis.api.projectStructure.KaModule]s. This avoids the
 *   need to call every single subordinate symbol provider.
 * - A small Caffeine cache can avoid most index accesses for classes, because many names are requested multiple times, with a minor memory
 *   footprint.
 *
 * @param declarationProvider The declaration provider must have a scope which combines the scopes of the individual [providers].
 *
 * @param packageProviderForKotlinPackages This package provider should be combined from all [providers] which allow `kotlin` packages (see
 *  [LLKotlinSymbolProvider.allowKotlinPackage]). It may be `null` if no such provider exists. See [hasPackage] for a use case.
 */
internal class LLCombinedKotlinSymbolProvider private constructor(
    session: FirSession,
    project: Project,
    providers: List<LLKotlinSymbolProvider>,
    private val declarationProvider: KotlinDeclarationProvider,
    private val packageProvider: KotlinPackageProvider,
    private val packageProviderForKotlinPackages: KotlinPackageProvider?,
) : LLSelectingCombinedSymbolProvider<LLKotlinSymbolProvider>(session, project, providers) {
    override val symbolNamesProvider: FirSymbolNamesProvider = FirCompositeCachedSymbolNamesProvider.fromSymbolProviders(session, providers)

    private val classifierCache = NullableCaffeineCache<ClassId, FirClassLikeSymbol<*>> {
        it
            .maximumSize(500)
            .withStatsCounter(LLStatisticsService.getInstance(project)?.symbolProviders?.combinedSymbolProviderCacheStatsCounter)
    }

    override fun getClassLikeSymbolByClassId(classId: ClassId): FirClassLikeSymbol<*>? {
        if (!symbolNamesProvider.mayHaveTopLevelClassifier(classId)) return null

        return classifierCache.getOrPut(classId) { computeClassLikeSymbolByClassId(it) }
    }

    private fun computeClassLikeSymbolByClassId(classId: ClassId): FirClassLikeSymbol<*>? {
        val candidates = declarationProvider.getAllClassesByClassId(classId) + declarationProvider.getAllTypeAliasesByClassId(classId)
        val (ktClass, provider) = selectFirstElementInClasspathOrder(candidates) { it } ?: return null

        // We've picked the symbol provider via the `ktClass`, so `ktClass` must be contained in the symbol provider's module.
        @OptIn(LLModuleSpecificSymbolProviderAccess::class)
        return provider.getClassLikeSymbolByClassId(classId, ktClass)
    }

    @FirSymbolProviderInternals
    override fun getTopLevelCallableSymbolsTo(destination: MutableList<FirCallableSymbol<*>>, packageFqName: FqName, name: Name) {
        forEachCallableProvider(
            packageFqName,
            name,
            declarationProvider::getTopLevelCallables,
        ) { callableId, callables ->
            getTopLevelCallableSymbolsTo(destination, callableId, callables)
        }
    }

    @FirSymbolProviderInternals
    override fun getTopLevelFunctionSymbolsTo(destination: MutableList<FirNamedFunctionSymbol>, packageFqName: FqName, name: Name) {
        forEachCallableProvider(packageFqName, name, declarationProvider::getTopLevelFunctions) { callableId, functions ->
            getTopLevelFunctionSymbolsTo(destination, callableId, functions)
        }
    }

    @FirSymbolProviderInternals
    override fun getTopLevelPropertySymbolsTo(destination: MutableList<FirPropertySymbol>, packageFqName: FqName, name: Name) {
        forEachCallableProvider(packageFqName, name, declarationProvider::getTopLevelProperties) { callableId, properties ->
            getTopLevelPropertySymbolsTo(destination, callableId, properties)
        }
    }

    /**
     * Calls [provide] on those providers which can contribute a callable of the given name.
     *
     * We cannot use [KotlinDeclarationProvider.getTopLevelCallableFiles] like [LLKotlinSourceSymbolProvider][org.jetbrains.kotlin.analysis.low.level.api.fir.symbolProviders.LLKotlinSourceSymbolProvider]
     * for optimization because this approach only works for sources. Stub-based library symbol providers shouldn't access callables from
     * [KtFile][org.jetbrains.kotlin.psi.KtFile]s.
     */
    private inline fun <A : KtCallableDeclaration> forEachCallableProvider(
        packageFqName: FqName,
        name: Name,
        getCallables: (CallableId) -> Collection<A>,
        provide: LLKotlinSymbolProvider.(CallableId, Collection<A>) -> Unit,
    ) {
        if (!symbolNamesProvider.mayHaveTopLevelCallable(packageFqName, name)) return

        val callableId = CallableId(packageFqName, name)

        getCallables(callableId)
            .groupBy { getModule(it) }
            .forEach { (module, callables) ->
                // If `module` cannot be found in the map, `callables` cannot be processed by any of the available providers, because none
                // of them belong to the correct module. We can skip in that case because iterating through all providers wouldn't lead to
                // any results for `callables`.
                val provider = getProviderByModule(module) ?: return@forEach
                provider.provide(callableId, callables)
            }
    }

    override fun hasPackage(fqName: FqName): Boolean {
        val hasPackage = if (fqName.startsWith(StandardNames.BUILT_INS_PACKAGE_NAME)) {
            // If a package is a `kotlin` package, `packageProvider` might find it via the scope of an individual symbol provider that
            // disallows `kotlin` packages. Hence, the combined `getPackage` would erroneously find a package it shouldn't be able to find,
            // because calling that individual symbol provider directly would result in `null` (as it disallows `kotlin` packages). The
            // `packageProviderForKotlinPackages` solves this issue by including only scopes from symbol providers which allow `kotlin`
            // packages.
            packageProviderForKotlinPackages?.doesKotlinOnlyPackageExist(fqName) == true
        } else {
            packageProvider.doesKotlinOnlyPackageExist(fqName)
        }

        // Regarding caching `hasPackage`: The static (standalone) package provider precomputes its packages, while the IDE package provider
        // caches the results itself. Hence, it's currently unnecessary to provide another layer of caching here.
        return hasPackage
    }

    override fun estimateSymbolCacheSize(): Long = classifierCache.estimatedSize

    companion object {
        fun merge(session: LLFirSession, project: Project, providers: List<LLKotlinSymbolProvider>): FirSymbolProvider? =
            if (providers.size > 1) {
                val declarationProvider = project.mergeDeclarationProviders(providers.map { it.declarationProvider })

                val packageProvider = project.mergePackageProviders(providers.map { it.packageProvider })

                val packageProviderForKotlinPackages = providers
                    .filter { it.allowKotlinPackage }
                    .takeIf { it.isNotEmpty() }
                    ?.map { it.packageProvider }
                    ?.let(project::mergePackageProviders)

                LLCombinedKotlinSymbolProvider(
                    session,
                    project,
                    providers,
                    declarationProvider,
                    packageProvider,
                    packageProviderForKotlinPackages,
                )
            } else providers.singleOrNull()
    }
}

/**
 * Callables are provided very rarely (compared to functions/properties individually), so it's okay to hit indices twice here.
 */
private fun KotlinDeclarationProvider.getTopLevelCallables(callableId: CallableId): List<KtCallableDeclaration> =
    getTopLevelFunctions(callableId) + getTopLevelProperties(callableId)
