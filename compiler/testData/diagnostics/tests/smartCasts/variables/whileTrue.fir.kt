// RUN_PIPELINE_TILL: FRONTEND
fun x(): Boolean { return true }

public fun foo(pp: Any): Int {
    var p = pp
    while(true) {
        (p as String).length
        if (x()) break
        p = 42
    }
    // Smart cast is NOT possible here
    // (we could provide it but p = 42 makes it difficult to understand)
    return p.<!FUNCTION_EXPECTED!>length<!>()
}

/* GENERATED_FIR_TAGS: asExpression, assignment, break, functionDeclaration, ifExpression, integerLiteral, localProperty,
propertyDeclaration, smartcast, whileLoop */
