/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.platform.permissions

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analysis.api.platform.KaEngineService

/**
 * [KaAnalysisPermissionChecker] is an *engine service* which allows checking whether analysis is currently allowed.
 */
public interface KaAnalysisPermissionChecker : KaEngineService {
    public fun isAnalysisAllowed(): Boolean

    public fun getRejectionReason(): String

    public companion object {
        public fun getInstance(project: Project): KaAnalysisPermissionChecker =
            project.getService(KaAnalysisPermissionChecker::class.java)
    }
}
