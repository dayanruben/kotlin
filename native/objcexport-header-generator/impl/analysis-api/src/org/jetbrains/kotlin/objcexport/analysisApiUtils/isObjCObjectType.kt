/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.objcexport.analysisApiUtils

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.types.KaType
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.name.NativeStandardInteropNames

context(KaSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
internal fun KaType.isObjCObjectType(): Boolean {
    val symbol = this.symbol

    if (symbol != null) {
        if (symbol.classId == NativeStandardInteropNames.objCObjectClassId) {
            return true
        }

        if (symbol is KaClassSymbol) {
            return symbol.superTypes.any { it.isObjCObjectType() }
        }
    }

    return false
}
