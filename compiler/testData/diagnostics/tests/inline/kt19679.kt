// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
inline fun test(s: () -> Unit, <!NULLABLE_INLINE_PARAMETER!>p: (() -> Unit)?<!>) {
    s()
    p?.invoke()
}

/* GENERATED_FIR_TAGS: functionDeclaration, functionalType, inline, nullableType, safeCall */
