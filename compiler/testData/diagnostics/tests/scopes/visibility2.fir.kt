// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_VARIABLE
// JAVAC_EXPECTED_FILE
// WITH_EXTRA_CHECKERS
// LANGUAGE: -ForbidInferOfInvisibleTypeAsReifiedVarargOrReturnType

//FILE:a.kt
package a

private open class A {
    fun bar() {}
}

private fun foo() {}

fun <!EXPOSED_FUNCTION_RETURN_TYPE!>makeA<!>() = A()

private object PO {}

//FILE:b.kt
//+JDK
package b

import a.<!INVISIBLE_REFERENCE!>A<!>
import a.<!INVISIBLE_REFERENCE!>foo<!>
import a.makeA
import a.<!INVISIBLE_REFERENCE!>PO<!>

fun test() {
    val y = <!INFERRED_INVISIBLE_RETURN_TYPE_WARNING!>makeA()<!>
    y.<!INVISIBLE_REFERENCE!>bar<!>()
    <!INVISIBLE_REFERENCE!>foo<!>()

    val u : <!INVISIBLE_REFERENCE!>A<!> = <!INVISIBLE_REFERENCE!>A<!>()
    val a : java.util.Arrays.<!INVISIBLE_REFERENCE!>ArrayList<!><Int>;

    val po = <!INVISIBLE_REFERENCE!>PO<!>
}

class B : <!EXPOSED_SUPER_CLASS, INVISIBLE_REFERENCE, INVISIBLE_REFERENCE!>A<!>() {}

class Q {
    class W {
        fun foo() {
            val y = <!INFERRED_INVISIBLE_RETURN_TYPE_WARNING!>makeA()<!> //assure that 'makeA' is visible
        }
    }
}

//check that 'toString' can be invoked without specifying return type
class NewClass : java.util.ArrayList<<!PLATFORM_CLASS_MAPPED_TO_KOTLIN!>Integer<!>>() {
    <!REDUNDANT_VISIBILITY_MODIFIER!>public<!> override fun toString() = "a"
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, localProperty, nestedClass, objectDeclaration, override,
propertyDeclaration, stringLiteral */
