/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Scheduler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.LowMemoryWatcher
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.impl.base.sessions.KaBaseSessionProvider
import org.jetbrains.kotlin.analysis.api.lifetime.KaLifetimeToken
import org.jetbrains.kotlin.analysis.api.permissions.KaAnalysisPermissionRegistry
import org.jetbrains.kotlin.analysis.api.platform.lifetime.KotlinReadActionConfinementLifetimeToken
import org.jetbrains.kotlin.analysis.api.platform.projectStructure.KotlinProjectStructureProvider
import org.jetbrains.kotlin.analysis.api.projectStructure.KaDanglingFileModule
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModule
import org.jetbrains.kotlin.analysis.api.projectStructure.isStable
import org.jetbrains.kotlin.analysis.low.level.api.fir.LLFirInternals
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getFirResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.structure.LLFirDeclarationModificationService
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSessionInvalidationListener
import org.jetbrains.kotlin.psi.KtElement
import java.time.Duration
import kotlin.reflect.KClass

/**
 * [KaFirSessionProvider] keeps [KaFirSession]s in a cache, which are actively invalidated with their associated underlying
 * [LLFirSession][org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSession]s.
 */
internal class KaFirSessionProvider(project: Project) : KaBaseSessionProvider(project) {
    /**
     * [KaFirSession]s must be weakly referenced to allow simultaneous garbage collection of an unused [KaFirSession] together with its
     * [LLFirSession][org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSession].
     *
     * The cache automatically evicts unused [KaFirSession]s when they haven't been accessed for 10 seconds. The configured scheduler
     * ensures that cleanup happens even when the cache is not accessed. This allows deterministic cleanup of the cache's entries. While the
     * [KaFirSession] can be garbage-collected without this, it also frees up the strong [KaModule] key, which can hold references to PSI.
     *
     * This behavior may lead to duplicate [KaFirSession]s for the same [KaModule] existing in the world (if a thread is using a single
     * [KaFirSession] for >10 seconds). However, there is no uniqueness requirement for [KaSession]s, so the behavior is legal.
     */
    private val cache: Cache<KaModule, KaSession> =
        Caffeine.newBuilder()
            .weakValues()
            .expireAfterAccess(Duration.ofSeconds(10))
            .scheduler(Scheduler.systemScheduler())
            .build()

    init {
        LowMemoryWatcher.register(::clearCaches, project)
    }

    override fun getAnalysisSession(useSiteElement: KtElement): KaSession {
        val module = KotlinProjectStructureProvider.getModule(project, useSiteElement, useSiteModule = null)
        return getAnalysisSession(module)
    }

    override fun getAnalysisSession(useSiteModule: KaModule): KaSession {
        if (useSiteModule is KaDanglingFileModule && !useSiteModule.isStable) {
            return createAnalysisSession(useSiteModule)
        }

        val identifier = tokenFactory.identifier
        identifier.flushPendingChanges(project)

        return cache.get(useSiteModule, ::createAnalysisSession) ?: error("`createAnalysisSession` must not return `null`.")
    }

    private fun createAnalysisSession(useSiteKtModule: KaModule): KaFirSession {
        val firResolveSession = useSiteKtModule.getFirResolveSession(project)
        val validityToken = tokenFactory.create(project, firResolveSession.useSiteFirSession.createValidityTracker())
        return KaFirSession.createAnalysisSessionByFirResolveSession(firResolveSession, validityToken)
    }

    override fun clearCaches() {
        cache.invalidateAll()
    }

    /**
     * Note: Races cannot happen because the listener is guaranteed to be invoked in a write action.
     */
    internal class SessionInvalidationListener(val project: Project) : LLFirSessionInvalidationListener {
        private val analysisSessionProvider: KaFirSessionProvider
            get() = getInstance(project) as? KaFirSessionProvider
                ?: error("Expected the analysis session provider to be a `${KaFirSessionProvider::class.simpleName}`.")

        override fun afterInvalidation(modules: Set<KaModule>) {
            modules.forEach { analysisSessionProvider.cache.invalidate(it) }
        }

        override fun afterGlobalInvalidation() {
            // Session invalidation events currently don't report whether library modules were included in the global invalidation. This is
            // by design to avoid iterating through the whole analysis session cache and to simplify the global session invalidation event.
            // Nevertheless, a `KaFirSession`'s validity is based on the underlying `LLFirSession`, so removed analysis sessions for
            // library modules might still be valid. This is not a problem, though, because analysis session caching is not required for
            // correctness, but rather a performance optimization.
            analysisSessionProvider.clearCaches()
        }
    }
}

private fun KClass<out KaLifetimeToken>.flushPendingChanges(project: Project) {
    if (this == KotlinReadActionConfinementLifetimeToken::class &&
        KaAnalysisPermissionRegistry.getInstance().isAnalysisAllowedInWriteAction &&
        ApplicationManager.getApplication().isWriteAccessAllowed
    ) {
        // We must flush modifications to publish local modifications into FIR tree
        @OptIn(LLFirInternals::class)
        LLFirDeclarationModificationService.getInstance(project).flushModifications()
    }
}
