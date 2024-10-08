// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY

// CHECK_BYTECODE_TEXT
// 1 java/lang/invoke/LambdaMetafactory

// FILE: inlineFunRef.kt

inline fun plusK(x: String) = x.toString() + "K"

fun box() = J(::plusK).apply("O")

// FILE: J.java
public interface J {
    public String apply(String x);
}
