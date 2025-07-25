// RUN_PIPELINE_TILL: FRONTEND
// CHECK_TYPE
// DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER
// FILE: test.kt

package test

class Outer<E> {
    inner class Inner<F, G> {
        inner class Inner2
        inner class Inner3<H>
    }

    class Nested<I> {
        inner class Inner4<K>
    }

    object Obj {
        class Nested2<J> {
            inner class Inner5<L>
        }
    }
}

// FILE: main.kt

import test.*;

class A
class B
class C
class D

fun ok1(): Outer<A>.Inner<B, C>.Inner2 = null!!
fun ok2(): Outer<A>.Inner<B, C>.Inner2 = null!!
fun ok22(): test.Outer<A>.Inner<B, C>.Inner3<D> = null!!
fun ok3(): Outer.Nested<A>.Inner4<B> = null!!
fun ok4(): Outer.Obj.Nested2<A>.Inner5<B> = null!!
fun ok5(): test.Outer.Obj.Nested2<A>.Inner5<B> = null!!

// All arguments are resolved
fun errorTypeWithArguments(): <!UNRESOLVED_REFERENCE!>Q<!><A>.<!DEBUG_INFO_MISSING_UNRESOLVED!>W<!><B, C, D>.<!DEBUG_INFO_MISSING_UNRESOLVED!>R<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>M<!> = null!!

fun error1(): Outer<A>.Inner<B>.Inner3<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><C, D><!> = null!!
fun error2(): Outer<A>.Inner<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><B, C, D><!>.Inner2 = null!!
fun error3(): <!WRONG_NUMBER_OF_TYPE_ARGUMENTS!>Outer<!>.Inner<A, B>.Inner3<C> = null!!

fun error4(): Outer<!TYPE_ARGUMENTS_FOR_OUTER_CLASS_WHEN_NESTED_REFERENCED!><A><!>.Nested<B>.Inner4<C> = null!!
fun error5(): Outer<!TYPE_ARGUMENTS_FOR_OUTER_CLASS_WHEN_NESTED_REFERENCED!><A><!>.Obj.Nested2<B>.Inner5<C> = null!!
fun error6(): Outer.Obj<!WRONG_NUMBER_OF_TYPE_ARGUMENTS!><A><!>.Nested2<B>.Inner5<C> = null!!

fun error7(): test<!TYPE_ARGUMENTS_NOT_ALLOWED!><String><!>.Outer.Obj.Nested2<A>.Inner5<B> = null!!

/* GENERATED_FIR_TAGS: checkNotNullCall, classDeclaration, funWithExtensionReceiver, functionDeclaration, functionalType,
infix, inner, nestedClass, nullableType, objectDeclaration, typeParameter, typeWithExtension */
