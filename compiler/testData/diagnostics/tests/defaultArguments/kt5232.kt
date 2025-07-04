// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
interface A {
    fun visit(a:Int, b:String="") : String = b + a
}

class B : A {
    override fun visit(a:Int, b:String) : String = b + a
}

class C : A {
    <!NOTHING_TO_OVERRIDE!>override<!> fun visit(a:Int) : String = "" + a
}

/* GENERATED_FIR_TAGS: additiveExpression, classDeclaration, functionDeclaration, interfaceDeclaration, override,
stringLiteral */
