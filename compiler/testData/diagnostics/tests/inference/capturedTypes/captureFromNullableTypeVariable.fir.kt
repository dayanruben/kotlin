// RUN_PIPELINE_TILL: FRONTEND
// CHECK_TYPE

fun <T : Any> Array<T?>.filterNotNull(): List<T> = throw Exception()

fun test1(a: Array<out Int?>) {
    val list = a.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>filterNotNull<!>()
    list checkType { _<List<Int>>() }
}

fun test2(vararg a: Int?) {
    val list = a.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>filterNotNull<!>()
    list checkType { _<List<Int>>() }
}

/* GENERATED_FIR_TAGS: classDeclaration, funWithExtensionReceiver, functionDeclaration, functionalType, infix,
lambdaLiteral, localProperty, nullableType, outProjection, propertyDeclaration, typeConstraint, typeParameter,
typeWithExtension, vararg */
