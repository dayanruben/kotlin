// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// ISSUE: KT-57247

typealias Aliased = String
annotation class Tag(vararg val tags: Aliased) // K1: ok, K2: INVALID_TYPE_OF_ANNOTATION_MEMBER

/* GENERATED_FIR_TAGS: annotationDeclaration, outProjection, primaryConstructor, propertyDeclaration,
typeAliasDeclaration, vararg */
