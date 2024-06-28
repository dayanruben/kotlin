/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.objcexport

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.symbols.KaClassKind
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaEnumEntrySymbol
import org.jetbrains.kotlin.backend.konan.objcexport.*
import org.jetbrains.kotlin.name.Name

/**
 * Note: At the time of writing this function (and comment) we found it easiest
 * to construct the functions manually. Potentially, there is a way to get those functions from
 * the Analysis API by requesting the combined member scope and looking for [KtSymbolOrigin.SOURCE_MEMBER_GENERATED].
 */
context(KaSession, KtObjCExportSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
internal fun KaClassSymbol.translateEnumMembers(): List<ObjCExportStub> {
    if (classKind != KaClassKind.ENUM_CLASS) return emptyList()
    return getEnumEntries() + listOf(getEnumValuesMethod(), getEnumEntriesProperty())
}

context(KaSession, KtObjCExportSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
private fun KaClassSymbol.getEnumEntries(): List<ObjCProperty> {
    val staticMembers = this.staticDeclaredMemberScope.callables.toList()
    return staticMembers.filterIsInstance<KaEnumEntrySymbol>().map { entry ->

        val entryName = entry.getEnumEntryName(false)
        val swiftName = entry.getEnumEntryName(true)
        ObjCProperty(
            name = entryName,
            comment = null,
            origin = null,
            type = entry.returnType.mapToReferenceTypeIgnoringNullability(),
            propertyAttributes = listOf("class", "readonly"),
            declarationAttributes = listOf(swiftNameAttribute(swiftName))
        )
    }
}

/**
 * [org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportTranslatorImpl.buildEnumValuesMethod]
 */
context(KaSession, KtObjCExportSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
private fun KaClassSymbol.getEnumValuesMethod(): ObjCMethod {
    val valuesFunctionSymbol = staticMemberScope.callables(Name.identifier("values")).firstOrNull()
    return ObjCMethod(
        comment = null,
        isInstanceMethod = false,
        returnType = valuesFunctionSymbol?.returnType?.translateToObjCReferenceType() ?: ObjCIdType,
        selectors = listOf("values"),
        parameters = emptyList(),
        attributes = listOf(swiftNameAttribute("values()")),
        origin = null
    )
}

/**
 * [org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportTranslatorImpl.buildEnumEntriesProperty]
 */
context(KaSession, KtObjCExportSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
private fun KaClassSymbol.getEnumEntriesProperty(): ObjCProperty {
    val entriesSymbol = staticMemberScope.callables(Name.identifier("entries")).firstOrNull()

    return ObjCProperty(
        name = "entries",
        comment = null,
        type = entriesSymbol?.returnType?.translateToObjCReferenceType() ?: ObjCIdType,
        propertyAttributes = listOf("class", "readonly"),
        declarationAttributes = listOf(swiftNameAttribute("entries")),
        origin = null,
        setterName = null,
        getterName = null
    )
}

context(KaSession, KtObjCExportSession)
@Suppress("CONTEXT_RECEIVERS_DEPRECATED")
private fun KaEnumEntrySymbol.getEnumEntryName(forSwift: Boolean): String {

    val propertyName: String = getObjCPropertyName().run {
        when {
            forSwift -> this.swiftName
            else -> this.objCName
        }
    }

    // FOO_BAR_BAZ -> fooBarBaz:
    val name = propertyName.split('_').mapIndexed { index, s ->
        val lower = s.lowercase()
        if (index == 0) lower else lower.replaceFirstChar(Char::uppercaseChar)
    }.joinToString("").toIdentifier()
    return name
}
