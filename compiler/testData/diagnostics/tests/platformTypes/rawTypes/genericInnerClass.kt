// DISABLE_JAVA_FACADE
// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_VARIABLE
// FILE: A.java

import java.util.*;

class A<T> {

    void foo(T x) {}

    public class Inner<E> {
        Inner(E x0, T x, List<T> y) {}

        void foo(E x0, T x, List<T> y) {}
        A<Map<E, T>> bar() {}
    }
}

// FILE: Test.java

class Test {
    static A rawAField = null;
}

// FILE: main.kt

val strList: List<String> = null!!

fun main() {
    val rawA = Test.rawAField
    var rawInner = rawA.Inner<Double>("", "", strList)
    rawInner.foo("", "", strList)
    rawInner.bar().foo("")
}

/* GENERATED_FIR_TAGS: checkNotNullCall, flexibleType, functionDeclaration, javaProperty, javaType, localProperty,
propertyDeclaration, stringLiteral */
