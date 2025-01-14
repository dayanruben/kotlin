// TARGET_BACKEND: NATIVE
// FILECHECK_STAGE: CStubs

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

open class EmptyContinuation(override val context: CoroutineContext = EmptyCoroutineContext) : Continuation<Any?> {
    companion object : EmptyContinuation()
    override fun resumeWith(result: Result<Any?>) { result.getOrThrow() }
}

suspend fun sUnit(): Unit = suspendCoroutineUninterceptedOrReturn { x ->
    println("sUnit")
    x.resume(Unit)
    COROUTINE_SUSPENDED
}

suspend fun sUnit2(): Unit = suspendCoroutineUninterceptedOrReturn { x ->
    println("sUnit2")
    x.resume(Unit)
    COROUTINE_SUSPENDED
}

suspend fun sInt(): Int = suspendCoroutineUninterceptedOrReturn { x ->
    println("sInt")
    x.resume(42)
    COROUTINE_SUSPENDED
}

suspend fun sInt2(): Int = suspendCoroutineUninterceptedOrReturn { x ->
    println("sInt2")
    x.resume(42)
    COROUTINE_SUSPENDED
}

fun builderUnit(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun builderInt(c: suspend () -> Int) {
    c.startCoroutine(EmptyContinuation)
}

// CHECK-LABEL: define ptr @"kfun:#box(){}kotlin.String"
fun box(): String {
    s1()
    s2()
    s3()
    s4()
    s5()
    s6()
    s7()
    s8()
    s9()
    s10()
    s11()
    s12()
    s13()
    s14()
    s15()
    s16()
    s17()
    s18(true)
    s19(true)
    s20(true)
    s21()
    s22()
    s23(true)
    s24()
    return "OK"
}

fun s1() {
    // CHECK-LABEL: define internal ptr @"kfun:s1${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s1${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        println("s1")
    }
    // CHECK-LABEL: epilogue:
}

fun s2() {
    // CHECK-LABEL: define internal ptr @"kfun:s2${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s2${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        println("s2")
        sUnit()
    }
    // CHECK-LABEL: epilogue:
}

fun s3() {
    // CHECK-LABEL: define internal ptr @"kfun:s3${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s3${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        println("s3")
        sUnit()
        return@builderUnit
    }
    // CHECK-LABEL: epilogue:
}

fun s4() {
    // CHECK-LABEL: define internal ptr @"kfun:s4${{[0-9]*}}.invoke
    builderInt {
        // CHECK-NOT: call void @"kfun:s4${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        println("s4")
        return@builderInt sInt()
    }
    // CHECK-LABEL: epilogue:
}

fun s5() {
    // CHECK-LABEL: define internal ptr @"kfun:s5${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s5${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        println("s5")
        run { sUnit() }
    }
    // CHECK-LABEL: epilogue:
}

fun s6() {
    // CHECK-LABEL: define internal ptr @"kfun:s6${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s6${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        run {
            println("s6")
            sUnit()
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s7() {
    // CHECK-LABEL: define internal ptr @"kfun:s7${{[0-9]*}}.invoke
    builderInt {
        // CHECK-NOT: call void @"kfun:s7${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        run {
            println("s7")
            sInt()
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s8() {
    // CHECK-LABEL: define internal ptr @"kfun:s8${{[0-9]*}}.invoke
    builderInt {
        // CHECK-NOT: call void @"kfun:s8${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        run {
            println("s8")
            return@builderInt sInt()
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s9() {
    // CHECK-LABEL: define internal ptr @"kfun:s9${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s9${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        run {
            println("s9-1")
            run {
                println("s9-2")
                sUnit()
            }
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s10() {
    // CHECK-LABEL: define internal ptr @"kfun:s10${{[0-9]*}}.invoke
    builderInt {
        // CHECK-NOT: call void @"kfun:s10${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        run {
            println("s10-1")
            return@builderInt run {
                println("s10-2")
                sInt()
            }
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s11() {
    // CHECK-LABEL: define internal ptr @"kfun:s11${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s11${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        println("s11")
        sUnit()
        return@builderUnit
    }
    // CHECK-LABEL: epilogue:
}

fun s12() {
    // CHECK-LABEL: define internal ptr @"kfun:s12${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s12${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        run {
            println("s12")
            sUnit()
            return@builderUnit
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s13() {
    // CHECK-LABEL: define internal ptr @"kfun:s13${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s13${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        run {
            println("s13")
            sUnit()
        }
        return@builderUnit
    }
    // CHECK-LABEL: epilogue:
}

fun s14() {
    // CHECK-LABEL: define internal ptr @"kfun:s14${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s14${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        run {
            println("s14")
            sUnit()
            return@run
        }
    }
    // CHECK-LABEL: epilogue:
}

@Suppress("UNREACHABLE_CODE")
fun s15() {
    // CHECK-LABEL: define internal ptr @"kfun:s15${{[0-9]*}}.invoke
    builderInt {
        // CHECK-NOT: call void @"kfun:s15${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        run {
            println("s15-1")
            return@builderInt run {
                println("s15-2")
                return@builderInt sInt()
            }
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s16() {
    // CHECK-LABEL: define internal ptr @"kfun:s16${{[0-9]*}}.invoke
    builderInt {
        // CHECK-NOT: call void @"kfun:s16${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        run outer@{
            println("s16-1")
            return@builderInt run inner@{
                println("s16-2")
                return@inner sInt()
            }
        }
    }
    // CHECK-LABEL: epilogue:
}

@Suppress("UNREACHABLE_CODE")
fun s17() {
    // CHECK-LABEL: define internal ptr @"kfun:s17${{[0-9]*}}.invoke
    builderInt {
        // CHECK-NOT: call void @"kfun:s17${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        return@builderInt run outer@{
            println("s17-1")
            return@builderInt run inner@{
                println("s17-2")
                return@outer sInt()
            }
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s18(f: Boolean) {
    // CHECK-LABEL: define internal ptr @"kfun:s18${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s18${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        if (f) {
            println("s18-1")
            sUnit()
        } else {
            println("s18-2")
            sUnit2()
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s19(f: Boolean) {
    // CHECK-LABEL: define internal ptr @"kfun:s19${{[0-9]*}}.invoke
    builderInt {
        // CHECK-NOT: call void @"kfun:s19${{[0-9]*}}COROUTINE${{[0-9]*}}.<init>
        if (f) {
            println("s19-1")
            return@builderInt sInt()
        } else {
            println("s19-2")
            return@builderInt sInt2()
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s20(f: Boolean) {
    // CHECK-LABEL: define internal ptr @"kfun:s20${{[0-9]*}}.invoke
    builderInt {
        // CHECK-NOT: call void @"kfun:s20${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        return@builderInt if (f) {
            println("s20-1")
            sInt()
        } else {
            println("s20-2")
            sInt2()
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s21() {
    // CHECK-LABEL: define internal ptr @"kfun:s21${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s21${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        try {
            println("s21")
        } catch (t: Throwable) {
            sUnit()
        }
    }
    // CHECK-LABEL: epilogue:
}

fun s22() {
    // CHECK-LABEL: define internal ptr @"kfun:s22${{[0-9]*}}.invoke
    builderInt {
        // CHECK-NOT: call void @"kfun:s22${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        try {
            println("s22")
        } catch (t: Throwable) {
            return@builderInt sInt()
        }
        return@builderInt sInt2()
    }
    // CHECK-LABEL: epilogue:
}

fun s23(f: Boolean) {
    // CHECK-LABEL: define internal ptr @"kfun:s23${{[0-9]*}}.invoke
    builderUnit {
        // CHECK-NOT: call void @"kfun:s23${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        val x = run {
            if (f) {
                println("s23")
                sUnit()
                return@builderUnit
            }
            42
        }
        println(x)
    }
    // CHECK-LABEL: epilogue:
}

fun s24() {
    // CHECK-LABEL: define internal ptr @"kfun:s24${{[0-9]*}}.invoke
    builderUnit {
        // CHECK: call void @"kfun:s24${{[0-9]*}}.$invokeCOROUTINE${{[0-9]*}}.<init>
        sInt()
    }
    // CHECK-LABEL: epilogue:
}
