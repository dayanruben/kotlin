// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// KT-307 Unresolved reference

open class AL {
    fun get(i : Int) : Any? = i
}

interface ALE<T> : <!INTERFACE_WITH_SUPERCLASS!>AL<!> {
    fun getOrNull(index: Int, value: T) : T {
        return get(index) <!UNCHECKED_CAST!>as? T<!> ?: value
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, elvisExpression, functionDeclaration, interfaceDeclaration, nullableType,
typeParameter */
