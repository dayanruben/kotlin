// RUN_PIPELINE_TILL: BACKEND
// FILE: Test.java
public class Test {
    public static int i1 = Integer.MAX_VALUE;
    public static final int i2 = Integer.MAX_VALUE;
    public final int i3 = Integer.MAX_VALUE;
    public int i4 = Integer.MAX_VALUE;
}


// FILE: A.kt
val a1: Int = Test.i1 + 1
val a2: Int = Test.i2 + 1
val a3: Int = Test().i3 + 1
val a4: Int = Test().i4 + 1

/* GENERATED_FIR_TAGS: additiveExpression, integerLiteral, javaFunction, javaProperty, javaType, propertyDeclaration */
