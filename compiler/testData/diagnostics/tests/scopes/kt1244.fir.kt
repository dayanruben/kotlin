// RUN_PIPELINE_TILL: FRONTEND
//KT-1244 Frontend allows access to private members of other classes

package kt1244

class A {
    private var a = ""
}

class B() {
    init {
        A().<!INVISIBLE_REFERENCE!>a<!> = "Hello"
    }
}

/* GENERATED_FIR_TAGS: assignment, classDeclaration, init, primaryConstructor, propertyDeclaration, stringLiteral */
