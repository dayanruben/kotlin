// RUN_PIPELINE_TILL: FRONTEND
// SKIP_FIR_DUMP
// LANGUAGE: +MultiPlatformProjects

// MODULE: common

// FILE: expect.kt

expect interface I {
    <!WRONG_MODIFIER_TARGET!>expect<!> typealias A<!SYNTAX!><!>
<!SYNTAX!><!>}

// FILE: expect1.kt

expect class E1 {
    class I
}

// FILE: expect2.kt

expect class E2 {
    class I
}

// FILE: expect3.kt

expect class E3 {
    class I
}

// FILE: expect4.kt

open class IBase {
    typealias A = String
    open fun foo(a: A) {}
}

expect class Base : IBase {
    override fun foo(a: IBase.A)
}

// MODULE: platform()()(common)

// FILE: actual.kt

actual interface <!NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS!>I<!> {
    typealias A = String
}

// FILE: actual1.kt

actual class E1 {
    actual typealias <!ACTUAL_WITHOUT_EXPECT("actual typealias I = Int; The following declaration is incompatible because actualization by nested type alias is prohibited:    expect class I : Any")!>I<!> = Int
}

// FILE: actual2.kt

class A {
    typealias I = Int
}

actual typealias <!NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS("actual typealias E2 = A;     expect class I : Any    The following declaration is incompatible because actualization by nested type alias is prohibited:        typealias I = Int")!>E2<!> = A

// FILE: actual3.kt

class B {
    class I
}

actual typealias E3 = B  // OK

// FILE: actual4.kt

actual class Base: IBase() {
    actual override fun foo(a: String) { }
}
