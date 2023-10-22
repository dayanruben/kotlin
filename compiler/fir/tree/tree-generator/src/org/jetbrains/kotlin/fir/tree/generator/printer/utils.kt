/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.printer

import org.jetbrains.kotlin.fir.tree.generator.context.AbstractFirTreeBuilder
import org.jetbrains.kotlin.fir.tree.generator.firImplementationDetailType
import org.jetbrains.kotlin.fir.tree.generator.model.*
import org.jetbrains.kotlin.fir.tree.generator.pureAbstractElementType
import org.jetbrains.kotlin.generators.tree.*
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

enum class ImportKind(val postfix: String) {
    Element(""), Implementation(".impl"), Builder(".builder")
}

fun Builder.collectImports(): List<String> {
    val parents = parents.mapNotNull { it.fullQualifiedName }
    val builderDsl = "org.jetbrains.kotlin.fir.builder.FirBuilderDsl"
    return when (this) {
        is LeafBuilder -> implementation.collectImports(
            parents,
            ImportKind.Builder,
        ) + implementation.fullQualifiedName!! + usedTypes.mapNotNull { it.fullQualifiedName } + builderDsl + "kotlin.contracts.*"
        is IntermediateBuilder -> {
            val fqns = buildList {
                addAll(parents)
                usedTypes.mapNotNullTo(this) { it.fullQualifiedName }
                for (field in allFields) {
                    if (field.invisibleField) continue
                    // TODO: Use recursive import collection for TypeRefs
                    field.typeRef.fullQualifiedName?.let(this::add)
                    (field.typeRef as? ParametrizedTypeRef<*, *>)?.args?.values?.mapNotNullTo(this) { it.fullQualifiedName }
                }

                add(materializedElement?.fullQualifiedName ?: throw IllegalStateException(type))
                add(builderDsl)
            }

            fqns.filterRedundantImports(packageName, ImportKind.Builder)
        }
    }.sorted()
}

fun Implementation.collectImports(base: List<String> = emptyList(), kind: ImportKind = ImportKind.Implementation): List<String> {
    return element.collectImportsInternal(
        base + listOf(element.fullQualifiedName)
                + usedTypes.mapNotNull { it.fullQualifiedName }
                + arbitraryImportables.mapNotNull { it.fullQualifiedName }
                + parents.mapNotNull { it.fullQualifiedName }
                + listOfNotNull(
            pureAbstractElementType.fullQualifiedName.takeIf { needPureAbstractElement },
            firImplementationDetailType.fullQualifiedName.takeIf { isPublic || requiresOptIn },
        ),
        kind,
    )
}

fun Element.collectImports(): List<String> {
    val baseTypes = elementParents.mapTo(mutableListOf()) { it.fullQualifiedName!! }
    baseTypes += AbstractFirTreeBuilder.baseFirElement.fullQualifiedName
    baseTypes += elementParents.flatMap { it.args.values }.mapNotNull { it.fullQualifiedName } // TODO: Use recursive import collection for TypeRefs
    if (needPureAbstractElement) {
        baseTypes += pureAbstractElementType.fullQualifiedName
    }
    baseTypes.addAll(otherParents.map { it.fullQualifiedName })
    return collectImportsInternal(
        baseTypes,
        ImportKind.Element,
    )
}

private fun Element.collectImportsInternal(base: List<String>, kind: ImportKind): List<String> {
    val fqns = base + allFields.mapNotNull { it.typeRef.fullQualifiedName } +
            allFields.flatMap { it.overridenTypes.mapNotNull { it.fullQualifiedName } + it.arbitraryImportables.mapNotNull { it.fullQualifiedName } } +
            allFields.flatMap { (it.typeRef as? ParametrizedTypeRef<*, *>)?.args?.values?.mapNotNull { it.fullQualifiedName } ?: emptyList() } + // TODO: Use recursive import collection for TypeRefs
            params.flatMap { it.bounds.mapNotNull { it.fullQualifiedName } }
    val result = fqns.filterRedundantImports(packageName, kind).toMutableList()

    if (allFields.any { it is FieldList && it.isMutableOrEmpty }) {
        result += when (kind) {
            ImportKind.Implementation -> listOf(
                "org.jetbrains.kotlin.fir.MutableOrEmptyList",
                "org.jetbrains.kotlin.fir.builder.toMutableOrEmpty"
            )
            ImportKind.Builder -> listOf("org.jetbrains.kotlin.fir.builder.toMutableOrEmpty")
            else -> emptyList()
        }
    }

    allFields.mapNotNull { it.optInAnnotation?.fullQualifiedName }.distinct().ifNotEmpty {
        result += this
    }

    if (allFields.any { it.name == "source" && it.withReplace }) {
        return (result + "org.jetbrains.kotlin.fir.FirImplementationDetail").distinct()
    }
    return result
}

private fun List<String>.filterRedundantImports(
    packageName: String,
    kind: ImportKind,
): List<String> {
    val realPackageName = "$packageName.${kind.postfix}"
    return filter { fqn ->
        !fqn.startsWith("kotlin.") && fqn.dropLastWhile { it != '.' } != realPackageName
    }.distinct().sorted() + "$VISITOR_PACKAGE.*"
}


val Field.isVal: Boolean
    get() = (this is FieldList && !isMutableOrEmpty) || (this is FieldWithDefault && origin is FieldList && !origin.isMutableOrEmpty) || !isMutable


fun Field.transformFunctionDeclaration(returnType: String): String {
    return transformFunctionDeclaration(name.replaceFirstChar(Char::uppercaseChar), returnType)
}

fun transformFunctionDeclaration(transformName: String, returnType: String): String {
    return "fun <D> transform$transformName(transformer: FirTransformer<D>, data: D): $returnType"
}

fun Field.replaceFunctionDeclaration(overridenType: TypeRefWithNullability? = null, forceNullable: Boolean = false): String {
    val capName = name.replaceFirstChar(Char::uppercaseChar)
    val type = overridenType ?: typeRef
    val typeWithNullable = if (forceNullable) type.copy(nullable = true) else type
    return "fun replace$capName(new$capName: ${typeWithNullable.typeWithArguments})"
}

fun Field.getMutableType(forBuilder: Boolean = false): TypeRef = when (this) {
    is FieldList -> when {
        isMutableOrEmpty && !forBuilder -> type(BASE_PACKAGE, "MutableOrEmptyList", kind = TypeKind.Class)
        isMutable -> StandardTypes.mutableList
        else -> StandardTypes.list
    }.withArgs(baseType).copy(nullable)
    is FieldWithDefault -> if (isMutable) origin.getMutableType() else typeRef
    else -> typeRef
}

fun Field.call(): String = if (nullable) "?." else "."

val Element.safeDecapitalizedName: String get() = if (name == "Class") "klass" else name.replaceFirstChar(Char::lowercaseChar)

val ImplementationWithArg.generics: String
    get() = argument?.let { "<${it.type}>" } ?: ""
