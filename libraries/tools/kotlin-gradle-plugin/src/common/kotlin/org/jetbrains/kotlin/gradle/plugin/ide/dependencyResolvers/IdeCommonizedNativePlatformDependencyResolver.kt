/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.ide.dependencyResolvers

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.commonizer.SharedCommonizerTarget
import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinDependency
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.ide.IdeDependencyResolver
import org.jetbrains.kotlin.gradle.targets.native.internal.commonizeNativeDistributionTask
import org.jetbrains.kotlin.gradle.targets.native.internal.commonizedNativeDistributionKlibsOrNull
import org.jetbrains.kotlin.gradle.targets.native.internal.commonizerTarget
import java.io.File

internal object IdeCommonizedNativePlatformDependencyResolver :
    IdeDependencyResolver, IdeDependencyResolver.WithBuildDependencies {

    override fun resolve(sourceSet: KotlinSourceSet): Set<IdeaKotlinDependency> {
        val project = sourceSet.project
        val commonizerTarget = sourceSet.commonizerTarget.getOrThrow() as? SharedCommonizerTarget ?: return emptySet()
        val klibs: Provider<List<File>> = project.commonizedNativeDistributionKlibsOrNull(commonizerTarget) ?: return emptySet()
        val konanPlatformLibsCacheService = project.ideKonanDistributionLibsService()

        return konanPlatformLibsCacheService.get().ideDependenciesOfSharedTarget(commonizerTarget, klibs)
    }

    override fun dependencies(project: Project): Iterable<Any> {
        return listOfNotNull(project.commonizeNativeDistributionTask)
    }
}
