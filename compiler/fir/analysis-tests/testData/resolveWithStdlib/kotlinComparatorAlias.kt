// RUN_PIPELINE_TILL: BACKEND
fun test_1() {
    val comp = Comparator<Int> { x, y -> 1 }
}

fun test_3(comparator: java.util.Comparator<Int>) {
    comparator.compare(1, 2)
}

fun test_4(comparator: Comparator<Int>) {
    comparator.compare(1, 2)
}

/* GENERATED_FIR_TAGS: flexibleType, functionDeclaration, integerLiteral, lambdaLiteral, localProperty,
propertyDeclaration */
