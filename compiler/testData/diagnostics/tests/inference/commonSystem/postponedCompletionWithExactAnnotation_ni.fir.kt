// RUN_PIPELINE_TILL: BACKEND
// DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -UNCHECKED_CAST

interface ISample

fun <K> elvisSimple(x: K?, y: K): K = y

@Suppress(<!ERROR_SUPPRESSION!>"INVISIBLE_REFERENCE"<!>, "INVISIBLE_MEMBER", "HIDDEN")
fun <K> elvisExact(x: K?, y: K): @kotlin.internal.Exact K = y

fun <T : Number> materialize(): T? = null
fun <T> Any?.materialize(): T = null as T

fun test(nullableSample: ISample, any: Any) {
    elvisSimple(
        nullableSample,
        materialize()
    )

    elvisSimple(
        elvisSimple(nullableSample, materialize()),
        any
    )

    elvisSimple(
        elvisExact(nullableSample, materialize()),
        any
    )

    val a: String? = null

    val x1: String? = run {
        a ?: a?.materialize()
    }

    val x2 = run {
        a ?: a?.materialize()
    }
}

/* GENERATED_FIR_TAGS: asExpression, elvisExpression, funWithExtensionReceiver, functionDeclaration,
interfaceDeclaration, intersectionType, lambdaLiteral, localProperty, nullableType, propertyDeclaration, safeCall,
smartcast, stringLiteral, typeConstraint, typeParameter */
