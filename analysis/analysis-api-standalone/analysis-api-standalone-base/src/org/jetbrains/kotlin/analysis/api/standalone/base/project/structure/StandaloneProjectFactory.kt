/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.standalone.base.project.structure

import com.intellij.codeInsight.ExternalAnnotationsManager
import com.intellij.codeInsight.InferredAnnotationsManager
import com.intellij.core.CoreJavaFileManager
import com.intellij.mock.MockProject
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.ServiceManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.file.impl.JavaFileManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.util.io.URLUtil.JAR_PROTOCOL
import com.intellij.util.io.URLUtil.JAR_SEPARATOR
import org.jetbrains.kotlin.analysis.project.structure.*
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.cli.jvm.index.JavaRoot
import org.jetbrains.kotlin.cli.jvm.index.JvmDependenciesIndexImpl
import org.jetbrains.kotlin.cli.jvm.index.SingleJavaFileRootsIndex
import org.jetbrains.kotlin.cli.jvm.modules.CliJavaModuleFinder
import org.jetbrains.kotlin.cli.jvm.modules.CliJavaModuleResolver
import org.jetbrains.kotlin.cli.jvm.modules.JavaModuleGraph
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.load.kotlin.MetadataFinderFactory
import org.jetbrains.kotlin.load.kotlin.VirtualFileFinderFactory
import org.jetbrains.kotlin.resolve.ModuleAnnotationsResolver
import org.jetbrains.kotlin.resolve.jvm.modules.JavaModuleResolver
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.kotlin.utils.addToStdlib.popLast
import java.nio.file.Path

object StandaloneProjectFactory {
    fun createProjectEnvironment(
        projectDisposable: Disposable,
        applicationDisposable: Disposable,
        compilerConfiguration: CompilerConfiguration = CompilerConfiguration(),
    ): KotlinCoreProjectEnvironment {
        val applicationEnvironment =
            KotlinCoreEnvironment.getOrCreateApplicationEnvironmentForTests(applicationDisposable, compilerConfiguration)

        return KotlinCoreProjectEnvironment(projectDisposable, applicationEnvironment).apply {
            registerJavaPsiFacade(project)
        }
    }

    private fun registerJavaPsiFacade(project: MockProject) {
        with(project) {
            registerService(
                CoreJavaFileManager::class.java,
                ServiceManager.getService(this, JavaFileManager::class.java) as CoreJavaFileManager
            )

            registerService(ExternalAnnotationsManager::class.java, MockExternalAnnotationsManager())
            registerService(InferredAnnotationsManager::class.java, MockInferredAnnotationsManager())
        }
    }

    fun registerServicesForProjectEnvironment(
        environment: KotlinCoreProjectEnvironment,
        projectStructureProvider: ProjectStructureProvider,
        modules: List<KtModule>,
        sourceFiles: List<PsiFileSystemItem>,
        languageVersionSettings: LanguageVersionSettings = latestLanguageVersionSettings,
        jdkHome: Path? = null,
    ) {
        val project = environment.project

        KotlinCoreEnvironment.registerProjectExtensionPoints(project.extensionArea)
        KotlinCoreEnvironment.registerProjectServices(project)

        project.registerService(ProjectStructureProvider::class.java, projectStructureProvider)
        initialiseVirtualFileFinderServices(environment, modules, sourceFiles, languageVersionSettings, jdkHome)

        project.setupHighestLanguageLevel()
    }

    private fun initialiseVirtualFileFinderServices(
        environment: KotlinCoreProjectEnvironment,
        modules: List<KtModule>,
        sourceFiles: List<PsiFileSystemItem>,
        languageVersionSettings: LanguageVersionSettings,
        jdkHome: Path?,
    ) {
        val project = environment.project

        val allSourceFileRoots = sourceFiles.map { JavaRoot(it.virtualFile, JavaRoot.RootType.SOURCE) }
        val libraryRoots = getAllBinaryRoots(modules, environment)
        libraryRoots.forEach { environment.addSourcesToClasspath(it.file) }

        val sourceAndLibraryRoots = buildList {
            addAll(libraryRoots)
            addAll(allSourceFileRoots)
        }

        val javaFileManager = project.getService(JavaFileManager::class.java) as KotlinCliJavaFileManagerImpl
        val javaModuleFinder = CliJavaModuleFinder(jdkHome?.toFile(), null, javaFileManager, project, null)

        javaFileManager.initialize(
            JvmDependenciesIndexImpl(sourceAndLibraryRoots),
            listOf(
                createPackagePartsProvider(project, libraryRoots, languageVersionSettings)
                    .invoke(ProjectScope.getLibrariesScope(project))
            ),
            SingleJavaFileRootsIndex(emptyList()),
            true
        )

        project.registerService(
            JavaModuleResolver::class.java,
            CliJavaModuleResolver(JavaModuleGraph(javaModuleFinder), emptyList(), javaModuleFinder.systemModules.toList(), project)
        )

        val finderFactory = CliVirtualFileFinderFactory(JvmDependenciesIndexImpl(sourceAndLibraryRoots), false)

        project.registerService(MetadataFinderFactory::class.java, finderFactory)
        project.registerService(VirtualFileFinderFactory::class.java, finderFactory)
    }

    fun findJvmRootsForJavaFiles(files: List<PsiJavaFile>): List<PsiDirectory> {
        if (files.isEmpty()) return emptyList()
        val result = mutableSetOf<PsiDirectory>()
        for (file in files) {
            val packageParts = file.packageName.takeIf { it.isNotEmpty() }?.split('.') ?: emptyList()
            var javaDir: PsiDirectory? = file.parent
            for (part in packageParts.reversed()) {
                if (javaDir?.name == part) {
                    javaDir = javaDir.parent
                } else {
                    // Error(ish): file package does not match file path.
                    // This could happen if, e.g., src/my/pkg/MyTest.java has package `test.pkg`.
                    // It is just best practice, not enforced by language spec.
                    // So, here, we just stop iterating upward the folder structure.
                    break
                }
            }
            javaDir?.let { result += it }
        }
        return result.toList()
    }

    fun getAllBinaryRoots(
        modules: List<KtModule>,
        environment: KotlinCoreProjectEnvironment
    ): List<JavaRoot> = withAllTransitiveDependencies(modules)
        .filterIsInstance<KtBinaryModule>()
        .flatMap { it.getBinaryRoots() }
        .mapNotNull { path ->
            val pathString = path.toAbsolutePath().toString()
            val jar =
                if (pathString.endsWith(JAR_PROTOCOL))
                    environment.environment.jarFileSystem.findFileByPath(pathString + JAR_SEPARATOR)
                else null
            if (jar == null) return@mapNotNull null
            JavaRoot(jar, JavaRoot.RootType.BINARY)
        }

    private fun withAllTransitiveDependencies(ktModules: List<KtModule>): List<KtModule> {
        val visited = hashSetOf<KtModule>()
        val stack = ktModules.toMutableList()
        while (stack.isNotEmpty()) {
            val module = stack.popLast()
            if (module in visited) continue
            visited += module
            for (dependency in module.allDependencies()) {
                if (dependency !in visited) {
                    stack += dependency
                }
            }
        }
        return visited.toList()
    }

    private fun KtModule.allDependencies(): List<KtModule> = buildList {
        addAll(allDirectDependencies())
        when (this) {
            is KtLibrarySourceModule -> {
                add(binaryLibrary)
            }
            is KtLibraryModule -> {
                addIfNotNull(librarySources)
            }
        }
    }

    fun createPackagePartsProvider(
        project: MockProject,
        libraryRoots: List<JavaRoot>,
        languageVersionSettings: LanguageVersionSettings = latestLanguageVersionSettings,
    ): (GlobalSearchScope) -> JvmPackagePartProvider = { scope ->
        JvmPackagePartProvider(languageVersionSettings, scope).apply {
            addRoots(libraryRoots, MessageCollector.NONE)
            (ModuleAnnotationsResolver
                .getInstance(project) as CliModuleAnnotationsResolver)
                .addPackagePartProvider(this)
        }
    }

    private val latestLanguageVersionSettings: LanguageVersionSettings =
        LanguageVersionSettingsImpl(LanguageVersion.LATEST_STABLE, ApiVersion.LATEST)
}