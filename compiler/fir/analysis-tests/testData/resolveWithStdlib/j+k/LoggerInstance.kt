// RUN_PIPELINE_TILL: BACKEND
// FULL_JDK
// FILE: Logger.java

public class Logger {
    private static Logger me = new Logger();

    public static Logger getInstance(String s) {
        return me;
    }

    public static Logger getInstance(Class c) {
        return me;
    }
}

// FILE: test.kt

fun test() {
    val logger = Logger.getInstance("test")
}

class MyTest {
    private val klass = this::class.java

    private val logger = Logger.getInstance(klass)
}

/* GENERATED_FIR_TAGS: capturedType, classDeclaration, classReference, flexibleType, functionDeclaration, javaFunction,
localProperty, outProjection, propertyDeclaration, stringLiteral, thisExpression */
