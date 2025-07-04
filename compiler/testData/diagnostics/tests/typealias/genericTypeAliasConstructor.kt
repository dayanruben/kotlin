// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
class C<T>(val x: T, val y: String) {
    constructor(x: T): this(x, "")
}

typealias GTC<T> = C<T>

val test1 = GTC<String>("", "")
val test2 = GTC<String>("", "")
val test3 = GTC<String>("")
val test4 = GTC<String>("")

/* GENERATED_FIR_TAGS: classDeclaration, nullableType, primaryConstructor, propertyDeclaration, secondaryConstructor,
stringLiteral, typeAliasDeclaration, typeAliasDeclarationWithTypeParameter, typeParameter */
