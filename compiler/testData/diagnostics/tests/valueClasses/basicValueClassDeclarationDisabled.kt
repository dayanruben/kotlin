// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// SKIP_JAVAC
// API_VERSION: 1.4
// LANGUAGE: -InlineClasses
// DIAGNOSTICS: -UNUSED_PARAMETER
// ALLOW_KOTLIN_PACKAGE

package kotlin.jvm

annotation class JvmInline

<!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION!>value<!> class Foo(val x: Int)

<!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION, WRONG_MODIFIER_TARGET!>value<!> annotation class InlineAnn
<!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION, WRONG_MODIFIER_TARGET!>value<!> object InlineObject
<!VALUE_CLASS_WITHOUT_JVM_INLINE_ANNOTATION, WRONG_MODIFIER_TARGET!>value<!> enum class InlineEnum

@JvmInline
value class NotVal(<!VALUE_CLASS_CONSTRUCTOR_NOT_FINAL_READ_ONLY_PARAMETER!>x: Int<!>)

/* GENERATED_FIR_TAGS: annotationDeclaration, classDeclaration, enumDeclaration, objectDeclaration, primaryConstructor,
propertyDeclaration, value */
