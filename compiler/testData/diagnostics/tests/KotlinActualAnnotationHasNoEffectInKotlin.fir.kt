// RUN_PIPELINE_TILL: FRONTEND
// WITH_KOTLIN_JVM_ANNOTATIONS

import kotlin.annotations.jvm.KotlinActual

@<!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>
fun foo() {
    val a: <!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!> = null!!
    val b: (<!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>) -> Unit = { x -> }
    val c: ((<!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>) -> Unit, (<!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>) -> Unit) -> ((<!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>) -> Unit) = { x, y -> { } }
    val d: (() -> <!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>, () -> <!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>) -> (() -> <!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>) = { x, y -> { null!! } }

    val e = <!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>::class
    val f = <!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>::<!CALLABLE_REFERENCE_TO_ANNOTATION_CONSTRUCTOR!>KotlinActual<!><!>
    val g = <!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual()<!>
}

typealias Duh = <!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>

@[<!KOTLIN_ACTUAL_ANNOTATION_HAS_NO_EFFECT_IN_KOTLIN!>KotlinActual<!>]
fun bar() {}

/* GENERATED_FIR_TAGS: checkNotNullCall, classReference, functionDeclaration, functionalType, javaCallableReference,
javaFunction, lambdaLiteral, localProperty, propertyDeclaration, typeAliasDeclaration */
