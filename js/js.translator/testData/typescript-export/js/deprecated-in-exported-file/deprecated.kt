// This file was generated automatically. See  generateTestDataForTypeScriptWithFileExport.kt
// DO NOT MODIFY IT MANUALLY.

// CHECK_TYPESCRIPT_DECLARATIONS
// RUN_PLAIN_BOX_FUNCTION
// SKIP_NODE_JS
// INFER_MAIN_MODULE
// MODULE: JS_TESTS
// FILE: deprecated.kt

@file:JsExport

package foo


@Deprecated("message 1")
fun foo() {}


@Deprecated("message 2")
val bar: String = "Test"


@Deprecated("message 3")
class TestClass


class AnotherClass @Deprecated("message 4") constructor(val value: String) {
    @JsName("fromNothing")
    @Deprecated("message 5") constructor(): this("Test")

    @JsName("fromInt")
    constructor(value: Int): this(value.toString())

    @Deprecated("message 6")
    fun foo() {}

    fun baz() {}

    @Deprecated("message 7")
    val bar: String = "Test"
}


interface TestInterface {
    @Deprecated("message 8")
    fun foo()
    fun bar()
    @Deprecated("message 9")
    val baz: String
}


object TestObject {
    @Deprecated("message 10")
    fun foo() {}
    fun bar() {}
    @Deprecated("message 11")
    val baz: String = "Test"
}