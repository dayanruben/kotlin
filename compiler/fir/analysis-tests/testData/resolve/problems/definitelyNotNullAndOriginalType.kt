// RUN_PIPELINE_TILL: FRONTEND
// Original problem was discovered in `backend.main/org/jetbrains/kotlin/codegen/inline/InlineCache.kt`

// FILE: SLRUMap.java

import org.jetbrains.annotations.NotNull;

public interface SLRUMap<V> {
    void takeV(@NotNull V value);
}

// FILE: main.kt

fun <V> SLRUMap<V>.getOrPut(value: V) {
    takeV(<!ARGUMENT_TYPE_MISMATCH!>value<!>)
}

/* GENERATED_FIR_TAGS: funWithExtensionReceiver, functionDeclaration, javaType, nullableType, typeParameter */
