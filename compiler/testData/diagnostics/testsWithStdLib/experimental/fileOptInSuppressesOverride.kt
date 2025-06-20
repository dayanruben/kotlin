// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
@file:OptIn(Marker::class)

@RequiresOptIn
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Marker

@Marker
fun experimental() {}

interface MyInterface {
    fun execute()
}

class MyClass : MyInterface {
    override fun execute() = experimental()
}

/* GENERATED_FIR_TAGS: annotationDeclaration, annotationUseSiteTargetFile, classDeclaration, classReference,
functionDeclaration, interfaceDeclaration, override */
