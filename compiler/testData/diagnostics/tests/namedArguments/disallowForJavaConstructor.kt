// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// FILE: A.java

public class A {
    public A(int x, String y) {}
}

// FILE: 1.kt

val test = A(<!NAMED_ARGUMENTS_NOT_ALLOWED!>x<!> = 1, <!NAMED_ARGUMENTS_NOT_ALLOWED!>y<!> = "2")

/* GENERATED_FIR_TAGS: integerLiteral, javaFunction, javaType, propertyDeclaration, stringLiteral */
