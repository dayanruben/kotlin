// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// WITH_STDLIB
// MODULE: m1-common
// FILE: common.kt

expect open class Base() {
    fun existingMethodInBase()
}

expect open class Foo : Base {
    fun existingMethod()
    val existingParam: Int
}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

actual typealias Base = BaseImpl

open class BaseImpl {
    fun existingMethodInBase() {}
    fun injected() {}
}

actual open class Foo : Base() {
    actual fun existingMethod() {}
    actual val existingParam: Int = 904
}

/* GENERATED_FIR_TAGS: actual, classDeclaration, expect, functionDeclaration, integerLiteral, primaryConstructor,
propertyDeclaration, typeAliasDeclaration */
