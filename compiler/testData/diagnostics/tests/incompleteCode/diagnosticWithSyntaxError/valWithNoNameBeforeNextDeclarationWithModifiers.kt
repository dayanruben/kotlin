// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
abstract class A {
    private val<!SYNTAX!><!>
    // private is parsed as val's identifier
    private fun foo1() {
    }

    private val<!SYNTAX!><!>
    protected abstract fun foo2()

    private val<!SYNTAX!><!>
    fun foo3() {
    }

    private val private<!SYNTAX!><!> fun foo() {}
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, propertyDeclaration */
