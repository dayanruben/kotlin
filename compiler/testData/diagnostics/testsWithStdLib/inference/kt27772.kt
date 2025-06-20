// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
fun <T> foo(resources: List<T>) {
    resources.map { runCatching { it } }.mapNotNull { it.getOrNull() }
}

fun <T: Any> bar(resources: List<T>) {
    resources.map { runCatching { it } }.mapNotNull { it.getOrNull() }
}

/* GENERATED_FIR_TAGS: dnnType, functionDeclaration, lambdaLiteral, nullableType, typeConstraint, typeParameter */
