// ISSUE: KT-71662

fun testStandardNavigation() {
    val resultA = pcla { otvOwner ->
        otvOwner.constrain(ScopeOwner(Value))
        // should fix OTv := ScopeOwner<Value> for scope navigation
        otvOwner.provide().<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE, DEBUG_INFO_UNRESOLVED_WITH_TARGET, UNRESOLVED_REFERENCE!>memberFunction<!>(TypeArgument)
        // expected: Interloper </: ScopeOwner<Value>
        otvOwner.constrain(Interloper)
    }
    // expected: ScopeOwner<Value>
    <!DEBUG_INFO_EXPRESSION_TYPE("BaseType")!>resultA<!>

    val resultB = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>pcla<!> { otvOwner ->
        otvOwner.constrain(ScopeOwner(Value))
        // should fix OTv := ScopeOwner<Value> for scope navigation
        <!BUILDER_INFERENCE_STUB_RECEIVER!>otvOwner.provide()<!>.<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>extensionFunction<!>(TypeArgument)
        // expected: Interloper </: ScopeOwner<Value>
        otvOwner.constrain(<!TYPE_MISMATCH!>Interloper<!>)
    }
    // expected: ScopeOwner<Value>
    <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE, DEBUG_INFO_EXPRESSION_TYPE("[Error type: Not found recorded type for pcla { otvOwner ->        otvOwner.constrain(ScopeOwner(Value))        // should fix OTv := ScopeOwner<Value> for scope navigation        otvOwner.provide().extensionFunction(TypeArgument)        // expected: Interloper </: ScopeOwner<Value>        otvOwner.constrain(Interloper)    }]")!>resultB<!>

    val resultC = pcla { otvOwner ->
        otvOwner.constrain(ScopeOwner(Value))
        // should fix OTv := ScopeOwner<Value> for scope navigation
        <!BUILDER_INFERENCE_STUB_RECEIVER!>otvOwner.provide()<!>.fix()
        // expected: Interloper </: ScopeOwner<Value>
        otvOwner.constrain(Interloper)
    }
    // expected: ScopeOwner<Value>
    <!DEBUG_INFO_EXPRESSION_TYPE("BaseType")!>resultC<!>
}

fun testSafeNavigation() {
    val resultA = pcla { otvOwner ->
        otvOwner.constrain(ScopeOwner.Nullable(Value))
        // should fix OTv := ScopeOwner<Value>? for scope navigation
        otvOwner.provide()?.<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE, DEBUG_INFO_UNRESOLVED_WITH_TARGET, UNRESOLVED_REFERENCE!>memberFunction<!>(TypeArgument)
        // expected: Interloper </: ScopeOwner<Value>?
        otvOwner.constrain(Interloper)
    }
    // expected: ScopeOwner<Value>?
    <!DEBUG_INFO_EXPRESSION_TYPE("BaseType?")!>resultA<!>

    val resultB = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>pcla<!> { otvOwner ->
        otvOwner.constrain(ScopeOwner.Nullable(Value))
        // should fix OTv := ScopeOwner<Value>? for scope navigation
        <!BUILDER_INFERENCE_STUB_RECEIVER!>otvOwner.provide()<!>?.<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>extensionFunction<!>(TypeArgument)
        // expected: Interloper </: ScopeOwner<Value>?
        otvOwner.constrain(<!TYPE_MISMATCH!>Interloper<!>)
    }
    // expected: ScopeOwner<Value>?
    <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE, DEBUG_INFO_EXPRESSION_TYPE("[Error type: Not found recorded type for pcla { otvOwner ->        otvOwner.constrain(ScopeOwner.Nullable(Value))        // should fix OTv := ScopeOwner<Value>? for scope navigation        otvOwner.provide()?.extensionFunction(TypeArgument)        // expected: Interloper </: ScopeOwner<Value>?        otvOwner.constrain(Interloper)    }]")!>resultB<!>

    val resultC = pcla { otvOwner ->
        otvOwner.constrain(ScopeOwner.Nullable(Value))
        // should fix OTv := ScopeOwner<Value>? for scope navigation
        <!BUILDER_INFERENCE_STUB_RECEIVER!>otvOwner.provide()<!>?.fix()
        // expected: Interloper </: ScopeOwner<Value>?
        otvOwner.constrain(Interloper)
    }
    // expected: ScopeOwner<Value>?
    <!DEBUG_INFO_EXPRESSION_TYPE("BaseType?")!>resultC<!>
}


class TypeVariableOwner<T> {
    fun constrain(subtypeValue: T) {}
    fun provide(): T = null!!
}

fun <OT> pcla(lambda: (TypeVariableOwner<OT>) -> Unit): OT = null!!

interface BaseType

object Value

class ScopeOwner<SOT>(value: SOT): BaseType {
    fun <A> memberFunction(arg: A) {}
    companion object {
        fun <SOT> Nullable(value: SOT): ScopeOwner<SOT>? = null
    }
}

fun <SOTB, B> ScopeOwner<SOTB>.extensionFunction(arg: B) {}

object TypeArgument

fun <C> C.fix() {}

object Interloper: BaseType
