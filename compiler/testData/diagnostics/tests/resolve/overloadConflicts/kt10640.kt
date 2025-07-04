// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
interface AutoCloseable
interface Closeable : AutoCloseable

fun <T1 : AutoCloseable, R1> T1.myUse(f: (T1) -> R1): R1 = f(this)
fun <T2 : Closeable, R2> T2.myUse(f: (T2) -> R2): R2 = f(this)

fun test1(x: Closeable) = x.myUse { 42 }
fun test2(x: Closeable) = x.myUse<Closeable, Int> { 42 }
fun test3(x: Closeable) = x.myUse<AutoCloseable, Int> { 42 } // TODO KT-10681

/* GENERATED_FIR_TAGS: funWithExtensionReceiver, functionDeclaration, functionalType, integerLiteral,
interfaceDeclaration, lambdaLiteral, nullableType, thisExpression, typeConstraint, typeParameter */
