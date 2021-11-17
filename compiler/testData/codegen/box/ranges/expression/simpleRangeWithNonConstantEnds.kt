// Auto-generated by org.jetbrains.kotlin.generators.tests.GenerateRangesCodegenTestData. DO NOT EDIT!
// WITH_STDLIB



fun box(): String {
    val list1 = ArrayList<Int>()
    val range1 = (1 + 2)..(10 - 1)
    for (i in range1) {
        list1.add(i)
        if (list1.size > 23) break
    }
    if (list1 != listOf<Int>(3, 4, 5, 6, 7, 8, 9)) {
        return "Wrong elements for (1 + 2)..(10 - 1): $list1"
    }

    val list2 = ArrayList<Int>()
    val range2 = (1.toByte() + 2.toByte()).toByte()..(10.toByte() - 1.toByte()).toByte()
    for (i in range2) {
        list2.add(i)
        if (list2.size > 23) break
    }
    if (list2 != listOf<Int>(3, 4, 5, 6, 7, 8, 9)) {
        return "Wrong elements for (1.toByte() + 2.toByte()).toByte()..(10.toByte() - 1.toByte()).toByte(): $list2"
    }

    val list3 = ArrayList<Int>()
    val range3 = (1.toShort() + 2.toShort()).toShort()..(10.toShort() - 1.toShort()).toShort()
    for (i in range3) {
        list3.add(i)
        if (list3.size > 23) break
    }
    if (list3 != listOf<Int>(3, 4, 5, 6, 7, 8, 9)) {
        return "Wrong elements for (1.toShort() + 2.toShort()).toShort()..(10.toShort() - 1.toShort()).toShort(): $list3"
    }

    val list4 = ArrayList<Long>()
    val range4 = (1L + 2L)..(10L - 1L)
    for (i in range4) {
        list4.add(i)
        if (list4.size > 23) break
    }
    if (list4 != listOf<Long>(3, 4, 5, 6, 7, 8, 9)) {
        return "Wrong elements for (1L + 2L)..(10L - 1L): $list4"
    }

    val list5 = ArrayList<Char>()
    val range5 = ("ace"[1])..("age"[1])
    for (i in range5) {
        list5.add(i)
        if (list5.size > 23) break
    }
    if (list5 != listOf<Char>('c', 'd', 'e', 'f', 'g')) {
        return "Wrong elements for (\"ace\"[1])..(\"age\"[1]): $list5"
    }

    return "OK"
}
