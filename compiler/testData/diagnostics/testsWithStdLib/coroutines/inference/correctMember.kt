// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// OPT_IN: kotlin.RequiresOptIn
// DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

@file:OptIn(ExperimentalTypeInference::class)

import kotlin.experimental.ExperimentalTypeInference

interface Controller<T> {
    suspend fun yield(t: T) {}

    fun justString(): String = ""

    fun <Z> generidFun(t: Z) = t
}

fun <S> generate(g: suspend Controller<S>.() -> Unit): S = TODO()

val test1 = generate {
    yield(justString())
}

val test2 = generate {
    yield(generidFun(2))
}

/* GENERATED_FIR_TAGS: annotationUseSiteTargetFile, classReference, functionDeclaration, functionalType, integerLiteral,
interfaceDeclaration, lambdaLiteral, nullableType, propertyDeclaration, stringLiteral, suspend, typeParameter,
typeWithExtension */
