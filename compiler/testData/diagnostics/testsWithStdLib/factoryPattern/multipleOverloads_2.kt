// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// LANGUAGE: +OverloadResolutionByLambdaReturnType
// ALLOW_KOTLIN_PACKAGE
// DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -UNUSED_EXPRESSION -OPT_IN_USAGE -EXPERIMENTAL_UNSIGNED_LITERALS
// ISSUE: KT-11265

// FILE: OverloadResolutionByLambdaReturnType.kt

package kotlin

annotation class OverloadResolutionByLambdaReturnType

// FILE: main.kt

import kotlin.OverloadResolutionByLambdaReturnType

@OverloadResolutionByLambdaReturnType
fun <T, R : Comparable<R>> Iterable<T>.myMaxOf(selector: (T) -> R): R = TODO()
@OverloadResolutionByLambdaReturnType
fun <T> Iterable<T>.myMaxOf(selector: (T) -> Double): Double = TODO()
@OverloadResolutionByLambdaReturnType
fun <T> Iterable<T>.myMaxOf(selector: (T) -> Float): Float = TODO()

fun Double.pow(v: Int): Double = this

fun test() {
    val value = listOf(1, 2, 3, 4, 5, 6).myMaxOf { -2.0.pow(it) }
    takeDouble(value)
}

fun takeDouble(value: Double) {}

/* GENERATED_FIR_TAGS: annotationDeclaration, funWithExtensionReceiver, functionDeclaration, functionalType,
integerLiteral, lambdaLiteral, localProperty, nullableType, propertyDeclaration, thisExpression, typeConstraint,
typeParameter, unaryExpression */
