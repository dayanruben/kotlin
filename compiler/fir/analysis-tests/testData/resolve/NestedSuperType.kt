// RUN_PIPELINE_TILL: BACKEND
package p

abstract class My {
    abstract class NestedOne : My() {
        abstract class NestedTwo : NestedOne() {

        }
    }
}

class Your : My() {
    class NestedThree : NestedOne()
}

/* GENERATED_FIR_TAGS: classDeclaration, nestedClass */
