// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -DUPLICATE_CLASS_NAMES
<!FUNCTION_DECLARATION_WITH_NO_NAME!>fun ()<!> {

}

<!FUNCTION_DECLARATION_WITH_NO_NAME!>fun Outer.()<!> {

}

val<!SYNTAX!><!> : Int = 1

class<!SYNTAX!><!> {

}

object<!SYNTAX!><!> {

}

interface<!SYNTAX!><!> {

}

enum class<!SYNTAX!><!> {

}

annotation class<!SYNTAX!><!> {

}

class Outer {
    <!FUNCTION_DECLARATION_WITH_NO_NAME!>fun ()<!> {

    }

    val<!SYNTAX!><!> : Int = 1

    class<!SYNTAX!><!> {

    }

    object<!SYNTAX!><!> {

    }

    interface<!SYNTAX!><!> {

    }

    enum class<!SYNTAX!><!> {

    }

    annotation class<!SYNTAX!><!> {

    }
}

fun outerFun() {
    fun () {

    }
    fun () {

    }
}

/* GENERATED_FIR_TAGS: annotationDeclaration, anonymousFunction, classDeclaration, enumDeclaration,
funWithExtensionReceiver, functionDeclaration, integerLiteral, interfaceDeclaration, nestedClass, objectDeclaration,
propertyDeclaration */
