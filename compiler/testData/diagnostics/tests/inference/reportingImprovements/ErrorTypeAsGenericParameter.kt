// RUN_PIPELINE_TILL: FRONTEND
package a

fun <T, R, S> foo(block: (T)-> R, second: (T)-> S) = block

fun main() {
    val fff = { x: Int -> <!UNRESOLVED_REFERENCE!>aaa<!> }
    foo(<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>fff<!>, { x -> x + 1 })
}

/* GENERATED_FIR_TAGS: additiveExpression, functionDeclaration, functionalType, integerLiteral, lambdaLiteral,
localProperty, nullableType, propertyDeclaration, typeParameter */
