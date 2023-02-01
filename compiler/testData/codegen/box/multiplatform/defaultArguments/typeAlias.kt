// !LANGUAGE: +MultiPlatformProjects
// WITH_STDLIB
// IGNORE_BACKEND_K2: JVM_IR, JS_IR, NATIVE
// FIR status: In FIR, declaring the same `expect` and `actual` classes in one compiler module is not possible (see KT-55177).

// FILE: common.kt

expect annotation class Foo(val z: String = "OK")

// FILE: platform.kt

actual typealias Foo = Foo2

annotation class Foo2 (val z: String = "OK")

@Foo
fun test() {}

fun box(): String {
    test()

    return "OK"
}