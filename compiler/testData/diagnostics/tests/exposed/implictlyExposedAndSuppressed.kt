// RUN_PIPELINE_TILL: FRONTEND
// RENDER_DIAGNOSTICS_FULL_TEXT
// LANGUAGE: -ReportExposedTypeForMoreCasesOfTypeParameterBounds, -ForbidInferOfInvisibleTypeAsReifiedVarargOrReturnType
// LATEST_LV_DIFFERENCE

// MODULE: a

internal interface Inter {
    fun foo() = 10
}

class Wrapper<T>(val it: T)

fun <T: Inter?> public(a: T & Any) = Wrapper(a)

@Suppress("EXPOSED_FUNCTION_RETURN_TYPE")
fun other() = public(object : Inter {})

// MODULE: b(a)

fun test() {
    other().it.foo() // ok in K1, invisible reference in K2
}

/* GENERATED_FIR_TAGS: anonymousObjectExpression, capturedType, classDeclaration, dnnType, functionDeclaration,
integerLiteral, interfaceDeclaration, nullableType, outProjection, primaryConstructor, propertyDeclaration,
stringLiteral, typeConstraint, typeParameter */
