// RUN_PIPELINE_TILL: BACKEND
fun x(): Boolean { return true }

public fun foo(p: String?, r: String?): Int {
    do {
        do {
            p!!.length
        } while (r == null)  
    } while (!x())
    // Auto cast possible
    r.length
    // Auto cast possible
    return p.length
}

/* GENERATED_FIR_TAGS: checkNotNullCall, doWhileLoop, equalityExpression, functionDeclaration, nullableType, smartcast */
