// JVM_DEFAULT_MODE: no-compatibility
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB

interface Test {
    fun test(): String {
        return inlineProp
    }

    private inline val inlineProp: String
        get() = "OK"

}

class TestClass : Test {

}

fun box(): String =
    TestClass().test()
