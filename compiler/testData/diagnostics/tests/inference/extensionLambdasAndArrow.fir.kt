// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_ANONYMOUS_PARAMETER

fun <T> select(vararg x: T) = x[0]

fun main() {
    val x1: String.() -> String = if (true) {{ this }} else {{ this }}
    val x2: String.() -> String = if (true) {{ -> this }} else {{ -> this }}
    val x3: () -> String = if (true) {{ -> "this" }} else {{ -> "this" }}
    val x4: String.() -> String = if (true) {{ str: String -> "this" }} else {{ str: String -> "this" }}
    val x41: String.(String) -> String = if (true) {{ str: String, str2: String -> "this" }} else {{ str: String, str2: String -> "this" }}
    val x42: String.(String) -> String = if (true) {{ str, <!CANNOT_INFER_VALUE_PARAMETER_TYPE!>str2<!> -> "this" }} else {{ str, <!CANNOT_INFER_VALUE_PARAMETER_TYPE!>str2<!> -> "this" }}
    val x5: String.() -> String = if (true) {<!ARGUMENT_TYPE_MISMATCH!>{ <!CANNOT_INFER_VALUE_PARAMETER_TYPE!>str<!> -> "this" }<!>} else {<!ARGUMENT_TYPE_MISMATCH!>{ <!CANNOT_INFER_VALUE_PARAMETER_TYPE!>str<!> -> "this" }<!>}
    val x6: String.() -> String = <!INITIALIZER_TYPE_MISMATCH!>if (true) {<!ARGUMENT_TYPE_MISMATCH!>{ <!CANNOT_INFER_VALUE_PARAMETER_TYPE!>str<!> -> "this" }<!>} else {{ "this" }}<!>
    val x7: String.() -> String = select({ -> this }, { -> this })
    val x8: String.() -> String = select({ this }, { this })
}

/* GENERATED_FIR_TAGS: capturedType, functionDeclaration, functionalType, ifExpression, integerLiteral, lambdaLiteral,
localProperty, nullableType, outProjection, propertyDeclaration, stringLiteral, thisExpression, typeParameter,
typeWithExtension, vararg */
