// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// CHECK_TYPE
// FILE: a.kt

package other

fun foo() {}

class A {
    fun bar() = 42
}

fun A.baz(x: String) {}

// FILE: b.kt

import kotlin.reflect.*

import other.foo as foofoo
import other.A as AA
import other.baz as bazbaz

fun main() {
    val x = ::foofoo
    val y = AA::bar
    val z = AA::bazbaz

    checkSubtype<KFunction0<Unit>>(x)
    checkSubtype<KFunction1<AA, Int>>(y)
    checkSubtype<KFunction2<AA, String, Unit>>(z)
}

/* GENERATED_FIR_TAGS: callableReference, classDeclaration, funWithExtensionReceiver, functionDeclaration,
functionalType, infix, integerLiteral, localProperty, nullableType, propertyDeclaration, typeParameter,
typeWithExtension */
