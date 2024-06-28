/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.objcexport

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaVariableSymbol
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportPropertyName


context(KaSession, KtObjCExportSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
fun KaVariableSymbol.getObjCPropertyName(): ObjCExportPropertyName {
    val resolveObjCNameAnnotation = resolveObjCNameAnnotation()
    val stringName = exportSessionSymbolName()
    val propertyName = stringName.mangleIfReservedObjCName()

    return ObjCExportPropertyName(
        objCName = resolveObjCNameAnnotation?.objCName ?: propertyName,
        swiftName = resolveObjCNameAnnotation?.swiftName ?: propertyName
    )
}
