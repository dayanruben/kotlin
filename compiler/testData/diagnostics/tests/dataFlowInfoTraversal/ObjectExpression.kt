// RUN_PIPELINE_TILL: FRONTEND

fun bar(x: Int): Int = x + 1

fun foo() {
    val x: Int? = null

    val a = object {
        fun baz() = bar(if (x == null) 0 else <!DEBUG_INFO_SMARTCAST!>x<!>)
        fun quux(): Int = if (x == null) <!DEBUG_INFO_CONSTANT, TYPE_MISMATCH!>x<!> else <!DEBUG_INFO_SMARTCAST!>x<!>
    }
}

/* GENERATED_FIR_TAGS: additiveExpression, anonymousObjectExpression, equalityExpression, functionDeclaration,
ifExpression, integerLiteral, localProperty, nullableType, propertyDeclaration, smartcast */
