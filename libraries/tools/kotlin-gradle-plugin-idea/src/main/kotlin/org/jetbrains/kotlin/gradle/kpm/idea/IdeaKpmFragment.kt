/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.kpm.idea

import org.jetbrains.kotlin.tooling.core.Extras
import java.io.Serializable

sealed interface IdeaKpmFragment : Serializable {
    val coordinates: IdeaKpmFragmentCoordinates
    val platforms: Set<IdeaKpmPlatform>
    val languageSettings: IdeaKpmLanguageSettings?
    val dependencies: List<IdeaKpmDependency>
    val sourceDirectories: List<IdeaKpmSourceDirectory>
    val resourceDirectories: List<IdeaKpmResourceDirectory>
    val extras: Extras
}

val IdeaKpmFragment.name get() = coordinates.fragmentName

@InternalKotlinGradlePluginApi
data class IdeaKpmFragmentImpl(
    override val coordinates: IdeaKpmFragmentCoordinates,
    override val platforms: Set<IdeaKpmPlatform>,
    override val languageSettings: IdeaKpmLanguageSettings?,
    override val dependencies: List<IdeaKpmDependency>,
    override val sourceDirectories: List<IdeaKpmSourceDirectory>,
    override val resourceDirectories: List<IdeaKpmResourceDirectory>,
    override val extras: Extras
) : IdeaKpmFragment {

    @InternalKotlinGradlePluginApi
    companion object {
        private const val serialVersionUID = 0L
    }
}

