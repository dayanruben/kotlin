// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
//FILE: Foo.java
public class Foo<T extends CharSequence> {
}

//FILE: Bar.java
public interface Bar {
    void f(Foo f);
}

//FILE: a.kt
class BarImpl: Bar {
    override fun f(f: Foo<*>?) {
        throw UnsupportedOperationException()
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, javaType, nullableType, override, starProjection */
