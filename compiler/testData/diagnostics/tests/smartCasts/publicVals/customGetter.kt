// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
public class X {
    private val x : String? = null
    public val y: CharSequence?
        get() = x?.subSequence(0, 1)
    public fun fn(): Int {
        if (y != null)
            // With non-default getter smartcast is not possible
            return <!SMARTCAST_IMPOSSIBLE!>y<!>.length
        else
            return 0
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, equalityExpression, functionDeclaration, getter, ifExpression, integerLiteral,
nullableType, propertyDeclaration, safeCall, smartcast */
