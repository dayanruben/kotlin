// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_VARIABLE
//KT-1270 Poor highlighting when trying to dereference a nullable reference

package kt1270

fun foo() {
    val sc = java.util.HashMap<String, SomeClass>()[""]
    val value = sc<!UNSAFE_CALL!>.<!>value
}

private class SomeClass() {
    val value : Int = 5
}

/* GENERATED_FIR_TAGS: classDeclaration, flexibleType, functionDeclaration, integerLiteral, javaFunction, localProperty,
nullableType, primaryConstructor, propertyDeclaration, stringLiteral */
