// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_VARIABLE
package test

fun nullableFun(): Int? = null
fun Int.foo() {}

val test1 = <!RESERVED_SYNTAX_IN_CALLABLE_REFERENCE_LHS!>nullableFun()<!>?::<!TYPE_MISMATCH, UNSAFE_CALL!>foo<!>

/* GENERATED_FIR_TAGS: funWithExtensionReceiver, functionDeclaration, nullableType, propertyDeclaration */
