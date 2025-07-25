// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

abstract class A {
    inner class InnerInA
}

abstract class B : A()

fun foo(a: A) {
    if (a is B) {
        val v = a::InnerInA
    }
}

/* GENERATED_FIR_TAGS: callableReference, classDeclaration, functionDeclaration, ifExpression, inner, isExpression,
localProperty, propertyDeclaration */
