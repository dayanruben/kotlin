// RUN_PIPELINE_TILL: FRONTEND
// NI_EXPECTED_FILE
// See EA-76890 / KT-10843: NPE during analysis
fun lambda(x : Int?) = x?.<!FUNCTION_CALL_EXPECTED, NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER, NO_VALUE_FOR_PARAMETER!>let<!> <!DEBUG_INFO_MISSING_UNRESOLVED!>l<!> {
    <!CANNOT_INFER_PARAMETER_TYPE!>y<!> ->
    if (<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>y<!> <!DEBUG_INFO_MISSING_UNRESOLVED!>><!> 0) return<!UNRESOLVED_REFERENCE!>@l<!> x
    <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>y<!>
}<!NOT_NULL_ASSERTION_ON_LAMBDA_EXPRESSION!>!!<!>

/* GENERATED_FIR_TAGS: checkNotNullCall, comparisonExpression, functionDeclaration, ifExpression, integerLiteral,
lambdaLiteral, nullableType, safeCall */
