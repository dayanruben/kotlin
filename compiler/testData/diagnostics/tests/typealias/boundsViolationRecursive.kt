// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

typealias R<T: <!BOUND_ON_TYPE_ALIAS_PARAMETER_NOT_ALLOWED!>List<<!UNRESOLVED_REFERENCE!>R<!>><!>> = List<T>

/* GENERATED_FIR_TAGS: typeAliasDeclaration, typeAliasDeclarationWithTypeParameter, typeConstraint, typeParameter */
