// KJS_WITH_FULL_RUNTIME
// MODULE: module1
// FILE: module1.kt

public fun f(s: String): String = "${s}: invoked from module"

public class A(val x: Int)

// MODULE: main(module1)
// FILE: main.kt

fun box(): String {

    assertEquals("A: invoked from module", f("A"))
    assertEquals(10, A(10).x)

    return "OK"
}
