// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_VARIABLE -INFIX_MODIFIER_REQUIRED

inline var value: (p: Int) -> String
    get() = {"123" }
    set(s: (p: Int) -> String) {
        s(11)
        s.invoke(11)
        s invoke 11

        val z = <!USAGE_IS_NOT_INLINABLE!>s<!>
    }

inline var value2: Int.(p: Int) -> String
    get() = {"123" }
    set(ext: Int.(p: Int) -> String) {
        11.ext(11)
        11.ext(11)

        val p = <!USAGE_IS_NOT_INLINABLE!>ext<!>
    }

/* GENERATED_FIR_TAGS: functionalType, getter, integerLiteral, lambdaLiteral, localProperty, propertyDeclaration, setter,
stringLiteral, typeWithExtension */
