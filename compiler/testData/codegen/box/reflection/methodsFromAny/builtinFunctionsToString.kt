// SKIP_JDK6
// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_REFLECT
// JVM_ABI_K1_K2_DIFF: KT-70625 K2 generates more correct signatures of callable references

import kotlin.test.assertEquals
import java.util.*

fun assertToString(s: String, x: Any) {
    assertEquals(s, x.toString())
}

fun box(): String {
    assertToString("fun kotlin.collections.Map<K, V>.getOrDefault(K, V): V", Map<*, *>::getOrDefault)
    assertToString("fun java.util.HashMap<K, V>.getOrDefault(K, V): V", HashMap<*, *>::getOrDefault)

    // TODO: uncomment once KT-11754 is fixed
    // assertToString("", MutableList<*>::removeAt)

    assertToString("fun kotlin.collections.Collection<E>.contains(E): kotlin.Boolean", Collection<*>::contains)
    assertToString("fun kotlin.collections.Set<E>.contains(E): kotlin.Boolean", Set<*>::contains)

    assertToString("fun kotlin.collections.Collection<E>.isEmpty(): kotlin.Boolean", Collection<*>::isEmpty)

    return "OK"
}
