// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_VARIABLE
// KT-3496 Type inference bug on y[""]

class B<T> {
    fun <S> x (y: B<Iterable<S>>) {
        val z: S = y[""] // does not work with [], but works with .get()
    }
    operator fun <S> get(s : String): S = throw Exception()
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, localProperty, nullableType, operator, propertyDeclaration,
stringLiteral, typeParameter */
