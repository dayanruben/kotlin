// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
import kotlin.reflect.*

fun <T> checkSubtype(t: T) = t

class A(var g: A) {
    val f: Int = 0

    fun test() {
        checkSubtype<KProperty1<A, Int>>(A::f)
        checkSubtype<KMutableProperty1<A, A>>(A::g)
    }
}

/* GENERATED_FIR_TAGS: callableReference, classDeclaration, functionDeclaration, integerLiteral, nullableType,
primaryConstructor, propertyDeclaration, typeParameter */
