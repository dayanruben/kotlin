// RUN_PIPELINE_TILL: BACKEND
// Changed in K2, see KT-57178

open class I {
    operator fun inc(): ST = ST()
}

class ST : I()

fun main() {
    var local = I()
    val x: ST = ++local
    val y: ST = <!TYPE_MISMATCH!>local<!>
}

/* GENERATED_FIR_TAGS: assignment, classDeclaration, functionDeclaration, incrementDecrementExpression, localProperty,
operator, propertyDeclaration, smartcast */
