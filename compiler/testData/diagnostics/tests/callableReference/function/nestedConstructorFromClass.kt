// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// CHECK_TYPE
// DIAGNOSTICS: -UNUSED_EXPRESSION
import kotlin.reflect.KFunction0

class A {
    class Nested
    
    fun main() {
        val x = ::Nested
        val y = A::Nested

        checkSubtype<KFunction0<Nested>>(x)
        checkSubtype<KFunction0<Nested>>(y)
    }
    
    companion object {
        fun main() {
            ::Nested
            val y = A::Nested

            checkSubtype<KFunction0<A.Nested>>(y)
        }
    }
}

class B {
    fun main() {
        ::<!UNRESOLVED_REFERENCE!>Nested<!>
        val y = A::Nested

        checkSubtype<KFunction0<A.Nested>>(y)
    }
}

/* GENERATED_FIR_TAGS: callableReference, classDeclaration, companionObject, funWithExtensionReceiver,
functionDeclaration, functionalType, infix, localProperty, nestedClass, nullableType, objectDeclaration,
propertyDeclaration, typeParameter, typeWithExtension */
