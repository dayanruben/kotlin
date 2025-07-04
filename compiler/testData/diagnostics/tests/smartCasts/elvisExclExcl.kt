// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// Based on KT-9100
fun test(x: Any?, y: Any?): Any {
    val z = x ?: y!!
    y<!UNSAFE_CALL!>.<!>hashCode()
    // !! / ?. is necessary here, because y!! above may not be executed
    y?.hashCode()
    y!!.hashCode()
    return z
}

/* GENERATED_FIR_TAGS: checkNotNullCall, elvisExpression, functionDeclaration, localProperty, nullableType,
propertyDeclaration, safeCall */
