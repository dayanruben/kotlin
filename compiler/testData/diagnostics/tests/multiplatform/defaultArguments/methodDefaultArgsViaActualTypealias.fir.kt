// IGNORE_FIR_DIAGNOSTICS
// RUN_PIPELINE_TILL: FIR2IR
// DIAGNOSTICS: -ACTUAL_WITHOUT_EXPECT
// MODULE: m1-common
// FILE: common.kt
expect class A {
    fun foo(p1: String = "common", p2: String = "common", p3: String)
}

expect class B {
    fun foo(s: String)
}

interface I {
    fun methodWithDefaultArg(s: String = "common")
}

expect class WithDefaultArgFromSuper : I {
    override fun methodWithDefaultArg(s: String)
}

<!EXPECT_ACTUAL_IR_INCOMPATIBILITY{JVM}!>expect<!> open class WithIncompatibility {
    fun foo(p: String = "common")
}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

class AImpl {
    fun foo(p1: String = "impl", p2: String = "impl", p3: String) {}
}

<!DEFAULT_ARGUMENTS_IN_EXPECT_WITH_ACTUAL_TYPEALIAS!>actual typealias <!EXPECT_ACTUAL_INCOMPATIBLE_CLASS_SCOPE!>A<!> = AImpl<!>

class BImpl {
    fun foo(s: String = "impl") {}
}

actual typealias <!EXPECT_ACTUAL_INCOMPATIBLE_CLASS_SCOPE!>B<!> = BImpl

class WithDefaultArgFromSuperImpl : I {
    override fun methodWithDefaultArg(s: String) {}
}

actual typealias WithDefaultArgFromSuper = WithDefaultArgFromSuperImpl

class WithIncompatibilityImpl {
    fun foo(p: String) {}
}

<!DEFAULT_ARGUMENTS_IN_EXPECT_WITH_ACTUAL_TYPEALIAS!>actual typealias <!EXPECT_ACTUAL_INCOMPATIBLE_MODALITY!>WithIncompatibility<!> = WithIncompatibilityImpl<!>

/* GENERATED_FIR_TAGS: actual, classDeclaration, expect, functionDeclaration, interfaceDeclaration, override,
stringLiteral, typeAliasDeclaration */
