// RUN_PIPELINE_TILL: BACKEND
// DIAGNOSTICS: -UNUSED_VARIABLE

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
fun <T, U: T> List<@kotlin.internal.Exact T>.firstTyped(): U = throw Exception()

fun test1(l: List<Number>) {

    val i: Int = l.firstTyped()

    val s: String = <!TYPE_MISMATCH!>l.<!TYPE_MISMATCH!>firstTyped<!>()<!>
}

/* GENERATED_FIR_TAGS: funWithExtensionReceiver, functionDeclaration, intersectionType, localProperty, nullableType,
propertyDeclaration, stringLiteral, typeConstraint, typeParameter */
