/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.objcexport

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.backend.konan.descriptors.arrayTypes
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCInstanceType
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCMethod
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCParameter
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCRawType
import org.jetbrains.kotlin.objcexport.analysisApiUtils.getSuperClassSymbolNotAny
import org.jetbrains.kotlin.objcexport.analysisApiUtils.hasExportForCompilerAnnotation
import org.jetbrains.kotlin.objcexport.analysisApiUtils.isVisibleInObjC

context(KaSession, KtObjCExportSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
fun KaClassSymbol.translateToObjCConstructors(): List<ObjCMethod> {

    /* Translate declared constructors */
    val result = declaredMemberScope
        .constructors
        .filter { !it.hasExportForCompilerAnnotation }
        .filter { it.isVisibleInObjC() }
        .sortedWith(StableCallableOrder)
        .flatMap { constructor ->
            val objCConstructor = constructor.buildObjCMethod()
            listOf(objCConstructor) + if (objCConstructor.name == "init") listOf(buildNewInitConstructor(constructor)) else emptyList()
        }
        .toMutableList()

    /* Create special 'alloc' constructors */
    if (this.classId?.asFqNameString() in arrayTypes ||
        classKind.isObject || classKind == KaClassKind.ENUM_CLASS
    ) {
        result.add(
            ObjCMethod(
                comment = null,
                origin = null,
                isInstanceMethod = false,
                returnType = ObjCInstanceType,
                selectors = listOf("alloc"),
                parameters = emptyList(),
                attributes = listOf("unavailable")
            )
        )

        result.add(
            ObjCMethod(
                comment = null,
                origin = getObjCExportStubOrigin(),
                isInstanceMethod = false,
                returnType = ObjCInstanceType,
                selectors = listOf("allocWithZone:"),
                parameters = listOf(ObjCParameter("zone", null, ObjCRawType("struct _NSZone *"), null)),
                attributes = listOf("unavailable")
            )
        )
    }

    // Hide "unimplemented" super constructors:
    getSuperClassSymbolNotAny()?.memberScope?.constructors.orEmpty()
        .filter { it.isVisibleInObjC() }
        .forEach { superClassConstructor ->
            val translatedSuperClassConstructor = superClassConstructor.buildObjCMethod(unavailable = true)
            if (result.none { it.name == translatedSuperClassConstructor.name }) {
                result.add(translatedSuperClassConstructor)
            }
        }

    return result
}

/**
 * Additional primary constructor which goes always after primary constructor ([ObjCMethod.name] == "init")
 */
context(KaSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
private fun buildNewInitConstructor(constructor: KaFunctionSymbol): ObjCMethod {
    return ObjCMethod(
        comment = null,
        origin = constructor.getObjCExportStubOrigin(),
        isInstanceMethod = false,
        returnType = ObjCInstanceType,
        selectors = listOf("new"),
        parameters = emptyList(),
        attributes = listOf(
            "availability(swift, unavailable, message=\"use object initializers instead\")"
        )
    )
}
