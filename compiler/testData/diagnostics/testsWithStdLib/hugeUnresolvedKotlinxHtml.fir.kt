// RUN_PIPELINE_TILL: FRONTEND
class A {
    fun bar() {
        <!UNRESOLVED_REFERENCE!>div<!> {
            <!UNRESOLVED_REFERENCE!>div<!> {
                <!UNRESOLVED_REFERENCE!>div<!> {
                    <!UNRESOLVED_REFERENCE!>div<!> {
                        <!UNRESOLVED_REFERENCE!>div<!> {
                            <!UNRESOLVED_REFERENCE!>div<!> {
                                <!UNRESOLVED_REFERENCE!>div<!> {
                                    <!UNRESOLVED_REFERENCE!>div<!> {
                                        <!UNRESOLVED_REFERENCE!>div<!> {
                                            <!UNRESOLVED_REFERENCE!>div<!> {
                                                <!UNRESOLVED_REFERENCE!>div<!> {
                                                    <!UNRESOLVED_REFERENCE!>div<!> {
                                                        <!UNRESOLVED_REFERENCE!>div<!> {
                                                            <!UNRESOLVED_REFERENCE!>div<!> {
                                                                <!UNRESOLVED_REFERENCE!>div<!> {
                                                                    <!UNRESOLVED_REFERENCE!>div<!> {
                                                                        <!UNRESOLVED_REFERENCE!>div<!> {
                                                                            <!UNRESOLVED_REFERENCE!>div<!> {
                                                                                <!UNRESOLVED_REFERENCE!>div<!> {
                                                                                    <!UNRESOLVED_REFERENCE!>div<!> {
                                                                                        <!UNRESOLVED_REFERENCE!>div<!> {
                                                                                            <!UNRESOLVED_REFERENCE!>div<!> {
                                                                                                <!UNRESOLVED_REFERENCE!>div<!> {
                                                                                                    <!UNRESOLVED_REFERENCE!>+<!>(<!UNRESOLVED_REFERENCE!>foo<!> ?: "")
                                                                                                    <!UNRESOLVED_REFERENCE!>+<!>(<!UNRESOLVED_REFERENCE!>foo<!> ?: "")
                                                                                                    <!UNRESOLVED_REFERENCE!>+<!>(<!UNRESOLVED_REFERENCE!>foo<!> ?: "")
                                                                                                    <!UNRESOLVED_REFERENCE!>+<!>(<!UNRESOLVED_REFERENCE!>foo<!> ?: "")
                                                                                                    <!UNRESOLVED_REFERENCE!>+<!>(<!UNRESOLVED_REFERENCE!>foo<!> ?: "")
                                                                                                    <!UNRESOLVED_REFERENCE!>+<!>(<!UNRESOLVED_REFERENCE!>foo<!> ?: "")
                                                                                                    <!UNRESOLVED_REFERENCE!>+<!>(<!UNRESOLVED_REFERENCE!>foo<!> ?: "")
                                                                                                    <!UNRESOLVED_REFERENCE!>+<!>(<!UNRESOLVED_REFERENCE!>foo<!> ?: "")
                                                                                                    <!UNRESOLVED_REFERENCE!>+<!>(<!UNRESOLVED_REFERENCE!>foo<!> ?: "")
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, elvisExpression, functionDeclaration, lambdaLiteral, stringLiteral,
unaryExpression */
