// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE
val la = { <!VALUE_PARAMETER_WITHOUT_EXPLICIT_TYPE!>a<!> -> }
val las = { a: Int -> }

val larg = { <!VALUE_PARAMETER_WITHOUT_EXPLICIT_TYPE!>a<!> -> }(123)
val twoarg = { <!VALUE_PARAMETER_WITHOUT_EXPLICIT_TYPE!>a<!>, b: String, <!VALUE_PARAMETER_WITHOUT_EXPLICIT_TYPE!>c<!> -> }(123, "asdf", 123)

/* GENERATED_FIR_TAGS: integerLiteral, lambdaLiteral, propertyDeclaration, stringLiteral */
