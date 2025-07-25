// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -NOTHING_TO_INLINE -UNUSED_PARAMETER

interface IBase {
    fun foo()
    fun bar()
    fun <T> qux(x: T)
}

class CDerived : IBase {
    <!OVERRIDE_BY_INLINE!>override inline final fun foo()<!> {}
    <!OVERRIDE_BY_INLINE!>override inline fun bar()<!> {}
    <!OVERRIDE_BY_INLINE!>override inline final fun <<!REIFIED_TYPE_PARAMETER_IN_OVERRIDE!>reified<!> T> qux(x: T)<!> {}

    class CNested : IBase {
        <!OVERRIDE_BY_INLINE!>override inline final fun foo()<!> {}
        <!OVERRIDE_BY_INLINE!>override inline fun bar()<!> {}
        <!OVERRIDE_BY_INLINE!>override inline final fun <<!REIFIED_TYPE_PARAMETER_IN_OVERRIDE!>reified<!> T> qux(x: T)<!> {}
    }

    val anObject = object : IBase {
        <!OVERRIDE_BY_INLINE!>override inline final fun foo()<!> {}
        <!OVERRIDE_BY_INLINE!>override inline fun bar()<!> {}
        <!OVERRIDE_BY_INLINE!>override inline final fun <<!REIFIED_TYPE_PARAMETER_IN_OVERRIDE!>reified<!> T> qux(x: T)<!> {}
    }

    fun aMethod() {
        class CLocal : IBase {
            <!OVERRIDE_BY_INLINE!>override inline final fun foo()<!> {}
            <!OVERRIDE_BY_INLINE!>override inline fun bar()<!> {}
            <!OVERRIDE_BY_INLINE!>override inline final fun <<!REIFIED_TYPE_PARAMETER_IN_OVERRIDE!>reified<!> T> qux(x: T)<!> {}
        }
    }
}

open class COpen : IBase {
    <!OVERRIDE_BY_INLINE!>override inline final fun foo()<!> {}
    override <!DECLARATION_CANT_BE_INLINED!>inline<!> fun bar() {}
    <!OVERRIDE_BY_INLINE!>override inline final fun <<!REIFIED_TYPE_PARAMETER_IN_OVERRIDE!>reified<!> T> qux(x: T)<!> {}

    open class COpenNested : IBase {
        <!OVERRIDE_BY_INLINE!>override inline final fun foo()<!> {}
        override <!DECLARATION_CANT_BE_INLINED!>inline<!> fun bar() {}
        <!OVERRIDE_BY_INLINE!>override inline final fun <<!REIFIED_TYPE_PARAMETER_IN_OVERRIDE!>reified<!> T> qux(x: T)<!> {}
    }

    val anObject = object : IBase {
        <!OVERRIDE_BY_INLINE!>override inline final fun foo()<!> {}
        <!OVERRIDE_BY_INLINE!>override inline fun bar()<!> {}
        <!OVERRIDE_BY_INLINE!>override inline final fun <<!REIFIED_TYPE_PARAMETER_IN_OVERRIDE!>reified<!> T> qux(x: T)<!> {}
    }

    fun aMethod() {
        open class COpenLocal : IBase {
            <!OVERRIDE_BY_INLINE!>override inline final fun foo()<!> {}
            override <!DECLARATION_CANT_BE_INLINED!>inline<!> fun bar() {}
            <!OVERRIDE_BY_INLINE!>override inline final fun <<!REIFIED_TYPE_PARAMETER_IN_OVERRIDE!>reified<!> T> qux(x: T)<!> {}
        }
    }
}

/* GENERATED_FIR_TAGS: anonymousObjectExpression, classDeclaration, functionDeclaration, inline, interfaceDeclaration,
localClass, nestedClass, nullableType, override, propertyDeclaration, reified, typeParameter */
