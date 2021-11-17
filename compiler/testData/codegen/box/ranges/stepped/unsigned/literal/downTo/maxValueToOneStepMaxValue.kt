// Auto-generated by GenerateSteppedRangesCodegenTestData. Do not edit!
// WITH_STDLIB
import kotlin.test.*

fun box(): String {
    val uintList = mutableListOf<UInt>()
    for (i in UInt.MAX_VALUE downTo 1u step Int.MAX_VALUE) {
        uintList += i
    }
    assertEquals(listOf(UInt.MAX_VALUE, 2147483648u, 1u), uintList)

    val ulongList = mutableListOf<ULong>()
    for (i in ULong.MAX_VALUE downTo 1uL step Long.MAX_VALUE) {
        ulongList += i
    }
    assertEquals(listOf(ULong.MAX_VALUE, 9223372036854775808uL, 1uL), ulongList)

    return "OK"
}