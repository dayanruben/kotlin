// RUN_PIPELINE_TILL: FRONTEND
// LANGUAGE: +AllowContractsForCustomFunctions +UseReturnsEffect
// OPT_IN: kotlin.contracts.ExperimentalContracts
// DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER

import kotlin.contracts.*

fun trueWhenString(x: Any?): Boolean {
    contract {
        returns(true) implies (x is String)
    }
    return x is String
}

fun trueWhenInt(x: Any?): Boolean {
    contract {
        returns(true) implies (x is Int)
    }
    return x is Int
}

fun falseWhenString(x: Any?): Boolean {
    contract {
        returns(false) implies (x is String)
    }
    return x !is String
}

fun falseWhenInt(x: Any?): Boolean {
    contract {
        returns(false) implies (x is Int)
    }
    return x !is Int
}

fun truetrue(x: Any?) {
    if (trueWhenString(x) || trueWhenInt(x)) {
        x.<!UNRESOLVED_REFERENCE!>length<!>
        x.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inc<!>()
    }
    else {
        x.<!UNRESOLVED_REFERENCE!>length<!>
        x.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inc<!>()
    }
}

fun truefalse(x: Any?) {
    if (trueWhenString(x) || falseWhenInt(x)) {
        x.<!UNRESOLVED_REFERENCE!>length<!>
        x.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inc<!>()
    }
    else {
        x.<!UNRESOLVED_REFERENCE!>length<!>
        <!DEBUG_INFO_SMARTCAST!>x<!>.inc()
    }
}

fun falsetrue(x: Any?) {
    if (falseWhenString(x) || trueWhenInt(x)) {
        x.<!UNRESOLVED_REFERENCE!>length<!>
        x.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inc<!>()
    }
    else {
        <!DEBUG_INFO_SMARTCAST!>x<!>.length
        x.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inc<!>()
    }
}

fun falsefalse(x: Any?) {
    if (falseWhenString(x) || falseWhenInt(x)) {
        x.<!UNRESOLVED_REFERENCE!>length<!>
        x.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>inc<!>()
    }
    else {
        <!DEBUG_INFO_SMARTCAST!>x<!>.length
        <!DEBUG_INFO_SMARTCAST!>x<!>.inc()
    }
}

/* GENERATED_FIR_TAGS: contractConditionalEffect, contracts, disjunctionExpression, functionDeclaration, ifExpression,
intersectionType, isExpression, lambdaLiteral, nullableType, smartcast */
