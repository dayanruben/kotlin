// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNCHECKED_CAST

interface I
interface I2 : I

fun <T : I> I.cast(): T? = this as? T

fun foo(i: I): I2? {
    return i.cast()!!
}

/* GENERATED_FIR_TAGS: checkNotNullCall, funWithExtensionReceiver, functionDeclaration, interfaceDeclaration,
nullableType, thisExpression, typeConstraint, typeParameter */
