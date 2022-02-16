// WITH_STDLIB
// TARGET_BACKEND: JS_IR
// FILE: main.js
Math.asinh = undefined;

// FILE: main.kt
import kotlin.math.asinh

fun box(): String {
    assertEquals(asinh(-1.0), -0.8813735870195429)
    assertEquals(asinh(0.0), 0.0)
    assertEquals(asinh(1.0), 0.8813735870195429)
    assertEquals(asinh(2.0), 1.4436354751788103)

    assertEquals(js("Math.asinh.called"), js("undefined"))

    return "OK"
}
