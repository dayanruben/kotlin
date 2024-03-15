// MODULE: commonjs
// TARGET_PLATFORM: JS
// FILE: commonjs.kt

expect interface ExternalInterface

external class ExternalClass: ExternalInterface

// MODULE: js()()(commonjs)
// TARGET_PLATFORM: JS
// FILE: js.kt

actual external interface ExternalInterface
