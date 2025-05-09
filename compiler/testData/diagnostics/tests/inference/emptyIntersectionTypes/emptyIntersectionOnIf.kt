// RUN_PIPELINE_TILL: BACKEND
// ISSUE: KT-57649

open class A
abstract class B {
    fun test(current: A): A? =
        if (current === this) current else null
}
