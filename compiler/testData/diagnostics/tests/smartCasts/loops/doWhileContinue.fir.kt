// RUN_PIPELINE_TILL: BACKEND
fun x(): Boolean { return true }

public fun foo(p: String?): Int {
    // See KT-6283
    do {
        p!!.length
        if (p == "abc") continue
    } while (!x())
    // p should be smart casted despite of continue
    return p.length
}

/* GENERATED_FIR_TAGS: checkNotNullCall, continue, doWhileLoop, equalityExpression, functionDeclaration, ifExpression,
nullableType, smartcast, stringLiteral */
