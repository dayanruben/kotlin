// RUN_PIPELINE_TILL: FRONTEND
@Target(AnnotationTarget.FIELD)
annotation class Field

<!WRONG_ANNOTATION_TARGET!>@Field<!>
annotation class Another

@Field
val x: Int = 42

<!WRONG_ANNOTATION_TARGET!>@Field<!>
val y: Int
    get() = 13

<!WRONG_ANNOTATION_TARGET!>@Field<!>
abstract class My(<!WRONG_ANNOTATION_TARGET!>@Field<!> arg: Int, @Field val w: Int) {
    @Field
    val x: Int = arg

    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    val y: Int
        get() = 0

    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    abstract val z: Int

    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    fun foo() {}

    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    val v: Int by <!UNRESOLVED_REFERENCE!>Delegates<!>.lazy { 42 }
}

enum class Your {
    @Field FIRST
}

interface His {
    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    val x: Int

    <!WRONG_ANNOTATION_TARGET!>@Field<!>
    val y: Int
        get() = 42
}

/* GENERATED_FIR_TAGS: annotationDeclaration, classDeclaration, enumDeclaration, enumEntry, functionDeclaration, getter,
integerLiteral, interfaceDeclaration, lambdaLiteral, primaryConstructor, propertyDeclaration, propertyDelegate,
starProjection */
