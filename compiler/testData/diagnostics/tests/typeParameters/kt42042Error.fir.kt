// RUN_PIPELINE_TILL: FRONTEND

sealed class Subtype<A1, B1> {
    abstract fun cast(value: A1): B1
    class Trivial<A2 : B2, B2> : Subtype<A2, B2>() {
        override fun cast(value: A2): B2 = value
    }
}

fun <A, B> unsafeCast(value: A): B {
    val proof: Subtype<A, B> = Subtype.<!CANNOT_INFER_PARAMETER_TYPE, CANNOT_INFER_PARAMETER_TYPE!>Trivial<!>()
    return proof.cast(value)
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, localProperty, nestedClass, nullableType, override,
propertyDeclaration, sealed, typeConstraint, typeParameter */
