// RUN_PIPELINE_TILL: FRONTEND
// LANGUAGE: +InlineClasses
// DIAGNOSTICS: -INLINE_CLASS_DEPRECATED

class Val {
    operator fun getValue(thisRef: Any?, kProp: Any?) = 1
}

class Var {
    operator fun getValue(thisRef: Any?, kProp: Any?) = 2
    operator fun setValue(thisRef: Any?, kProp: Any?, value: Int) {}
}


object ValObject {
    operator fun getValue(thisRef: Any?, kProp: Any?) = 1
}

object VarObject {
    operator fun getValue(thisRef: Any?, kProp: Any?) = 2
    operator fun setValue(thisRef: Any?, kProp: Any?, value: Int) {}
}

inline class Z(val data: Int) {
    val testVal <!DELEGATED_PROPERTY_INSIDE_VALUE_CLASS!>by Val()<!>
    var testVar <!DELEGATED_PROPERTY_INSIDE_VALUE_CLASS!>by Var()<!>

    val testValBySingleton <!DELEGATED_PROPERTY_INSIDE_VALUE_CLASS!>by ValObject<!>
    var testVarBySingleton <!DELEGATED_PROPERTY_INSIDE_VALUE_CLASS!>by VarObject<!>
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, integerLiteral, nullableType, objectDeclaration, operator,
primaryConstructor, propertyDeclaration, propertyDelegate, setter */
