// RUN_PIPELINE_TILL: BACKEND
fun <T> id(x: T): T = x
fun <K> select(x: K, y: K): K = TODO()

fun test() {
    select(id { it.inv() }, id<(Int) -> Unit> { })
}

/* GENERATED_FIR_TAGS: functionDeclaration, functionalType, lambdaLiteral, nullableType, typeParameter */
