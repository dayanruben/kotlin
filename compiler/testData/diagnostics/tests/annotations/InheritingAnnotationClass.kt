// RUN_PIPELINE_TILL: FRONTEND
annotation class AnnKlass

class Child : <!FINAL_SUPERTYPE, SUPERTYPE_NOT_INITIALIZED!>AnnKlass<!>

/* GENERATED_FIR_TAGS: annotationDeclaration, classDeclaration */
