// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// WITH_STDLIB
// DIAGNOSTICS: -UNUSED_VARIABLE

class Foo<T : Enum<T>>(val values: Array<T>)

fun foo(x: Array<out Enum<*>>) {
    val y = Foo(x)
}

/* GENERATED_FIR_TAGS: capturedType, classDeclaration, functionDeclaration, localProperty, outProjection,
primaryConstructor, propertyDeclaration, starProjection, typeConstraint, typeParameter */
