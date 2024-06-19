/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.objcexport

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.tooling.core.linearClosure

/**
 * Property needs to be translated and support special naming in 2 cases:
 * 1. It has no receiver type and no extension
 * 2. It is a property of an inner class
 * 3. Is is not extension of [mappedObjCTypes]
 *
 * See K1 [org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportMapperKt.isObjCProperty]
 */
context(KaSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
internal val KaPropertySymbol.isObjCProperty: Boolean
    get() {
        val isMappedReceiver = receiverParameter?.type?.isMappedObjCType == true
        val hasReceiver = receiverParameter != null && !isExtension
        if (isMappedReceiver) return false
        return !hasReceiver && !isPropertyInInnerClass
    }

context(KaSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
private val KaPropertySymbol.isPropertyInInnerClass: Boolean
    get() = linearClosure<KaSymbol> { symbol -> symbol.containingSymbol }
        .any { it is KaNamedClassSymbol && it.isInner }
