// RUN_PIPELINE_TILL: BACKEND
fun foo(): Any? {
    @Suppress("REDUNDANT_NULLABLE", "UNNECESSARY_NOT_NULL_ASSERTION")
    return ""!! as String??
}

/* GENERATED_FIR_TAGS: asExpression, checkNotNullCall, functionDeclaration, nullableType, stringLiteral */
