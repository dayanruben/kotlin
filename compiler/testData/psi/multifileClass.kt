// FILE: MultifileClass.kt
@file:[JvmName("MultifileClass") JvmMultifileClass]
package test

fun p1Fun() {}
fun String.p1ExtFun() {}
fun p1ExprFun(): Int = 0
fun p1FunWithParams(x: Int): Int { return x }

val p1Val: Int = 0
val String.p1ExtVal: Int get() = 0
var p1Var: Int = 0

@Deprecated("deprecated")
const val annotatedConstVal = 42

// FILE: SecondPart.kt
@file:[JvmName("MultifileClass") JvmMultifileClass]
package test

fun p2Fun() {}
fun String.p2ExtFun() {}
val p2Val: Int = 0
val String.p2ExtVal: Int get() = 0
