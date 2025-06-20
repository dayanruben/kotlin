// RUN_PIPELINE_TILL: FRONTEND
// CHECK_TYPE
// FILE: A.java
public interface A {
    String foo();
}

// FILE: main.kt

interface B {
    fun foo(): String?
}

interface C {
    fun foo(): String
}

fun foo(x: Any?) {
    if (x is A && x is B) {
        x.foo().checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><String>() }
        x.foo().checkType { _<String?>() }
    }

    if (x is B && x is A) {
        x.foo().checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><String>() }
        x.foo().checkType { _<String?>() }
    }

    if (x is A && x is C) {
        x.foo().checkType { _<String>() }
        x.foo().checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><String?>() }
    }

    if (x is C && x is A) {
        x.foo().checkType { _<String>() }
        x.foo().checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><String?>() }
    }

    if (x is A && x is B && x is C) {
        x.foo().checkType { _<String>() }
        x.foo().checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><String?>() }
    }

    if (x is B && x is A && x is C) {
        x.foo().checkType { _<String>() }
        x.foo().checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><String?>() }
    }

    if (x is B && x is C && x is A) {
        x.foo().checkType { _<String>() }
        x.foo().checkType { <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>_<!><String?>() }
    }
}

/* GENERATED_FIR_TAGS: andExpression, classDeclaration, funWithExtensionReceiver, functionDeclaration, functionalType,
ifExpression, infix, interfaceDeclaration, intersectionType, isExpression, javaType, lambdaLiteral, nullableType,
smartcast, typeParameter, typeWithExtension */
