// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
import kotlin.properties.ReadWriteProperty
import kotlin.properties.Delegates

class C {
    val `x$delegate`: ReadWriteProperty<Any, Any>? = null
    val x: String? by Delegates.notNull()
}

/* GENERATED_FIR_TAGS: classDeclaration, nullableType, propertyDeclaration, propertyDelegate */
