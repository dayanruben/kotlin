// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_ANONYMOUS_PARAMETER -UNUSED_VARIABLE
fun test(<!VALUE_PARAMETER_WITHOUT_EXPLICIT_TYPE!>a<!>, <!VALUE_PARAMETER_WITHOUT_EXPLICIT_TYPE!>b<!>, <!VALUE_PARAMETER_WITHOUT_EXPLICIT_TYPE!>c<!>) {

}

class A(a<!SYNTAX!><!>)

val bar = fun(<!VALUE_PARAMETER_WITHOUT_EXPLICIT_TYPE!>a<!>){}

val la = { <!VALUE_PARAMETER_WITHOUT_EXPLICIT_TYPE!>a<!> -> }
val las = { a: Int -> }

/* GENERATED_FIR_TAGS: anonymousFunction, classDeclaration, functionDeclaration, lambdaLiteral, primaryConstructor,
propertyDeclaration */
