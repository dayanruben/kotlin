// RUN_PIPELINE_TILL: BACKEND
class C {
    fun xf1(){}
    fun xf1(s: String){}
}

fun foo(p: (String) -> Unit){}

fun bar(c: C) {
    foo(c::xf1)
}

/* GENERATED_FIR_TAGS: callableReference, classDeclaration, functionDeclaration, functionalType */
