// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// FULL_JDK
// Issue: KT-37497

import java.util.*

object Bug1 {
    @JvmStatic
    fun main(args: Array<String>) {
        val list = object : ArrayList<String>(listOf("a")) {
            override fun sort(c: Comparator<in String>?) {
                super.sort(c)
                println(c)
            }
        }
        println(list)
    }
}

/* GENERATED_FIR_TAGS: anonymousObjectExpression, functionDeclaration, inProjection, localProperty, nullableType,
objectDeclaration, override, propertyDeclaration, stringLiteral, superExpression */
