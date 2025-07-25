// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// SKIP_TXT
import kotlin.coroutines.RestrictsSuspension

@RestrictsSuspension
interface Scope

suspend fun Scope.foo(
    a1: suspend Scope.() -> Unit,
    a2: suspend () -> Unit,
    a3: suspend String.() -> Unit,
) {
    a1()
    this.a1()
    <!ILLEGAL_RESTRICTED_SUSPENDING_FUNCTION_CALL!>a2<!>()

    "".<!ILLEGAL_RESTRICTED_SUSPENDING_FUNCTION_CALL!>a3<!>()
}

/* GENERATED_FIR_TAGS: funWithExtensionReceiver, functionDeclaration, functionalType, interfaceDeclaration,
stringLiteral, suspend, thisExpression, typeWithExtension */
