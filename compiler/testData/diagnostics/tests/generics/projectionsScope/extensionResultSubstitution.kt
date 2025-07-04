// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER

class A<T> {
    fun foo() = 1
}

interface B

public fun <E : B> E.bar() : A<out E> = null!!

fun baz(x: B) {
    x.bar().foo()
}

/* GENERATED_FIR_TAGS: checkNotNullCall, classDeclaration, funWithExtensionReceiver, functionDeclaration, integerLiteral,
interfaceDeclaration, nullableType, outProjection, typeConstraint, typeParameter */
