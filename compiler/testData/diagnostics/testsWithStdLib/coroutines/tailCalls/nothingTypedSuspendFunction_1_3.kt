// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// Tail calls are not allowed to be Nothing typed. See KT-15051
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

suspend fun suspendLogAndThrow(exception: Throwable): Nothing = suspendCoroutineUninterceptedOrReturn { c ->
    c.resumeWithException(exception)
    COROUTINE_SUSPENDED
}

/* GENERATED_FIR_TAGS: functionDeclaration, lambdaLiteral, nullableType, suspend */
