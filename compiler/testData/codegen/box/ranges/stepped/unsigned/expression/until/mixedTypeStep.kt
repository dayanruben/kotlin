// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    val uintList = mutableListOf<UInt>()
    val uintProgression = 1.toUByte() until 8.toUByte()
    for (i in uintProgression step 2) {
        uintList += i
    }
    assertEquals(listOf(1u, 3u, 5u, 7u), uintList)

    return "OK"
}