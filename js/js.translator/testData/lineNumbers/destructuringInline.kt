var log = ""

fun foo() {
    for (
        (
                q,
                w
                )
        in listOf(P("1", "2"))
    ) {
        log += q
        log += w
    }

    bar {
        (
                q,
                w
        ) ->
        log += q
        log += w
    }
}

fun bar(f: (P) -> Unit) {
    f(P("w", "e"))
}

class P(val a: String, val b: String)

inline operator fun P.component1() = a

inline operator fun P.component2() = b

// LINES(JS):    15 22 17 17 31 18 18 33 20 20 21 21 22 22 3 23 9 9 9 9 4 9 9 9 6 6 31 7 7 33 11 11 12 12 15 15 25 27 26 26 29 29 29 * 31 31 31 33 33 33 * 1 * 1
// LINES(JS_IR): 1 1 * 1 1 * 9 9 9 9 9 * 31 31 9 6 6 * 33 33 9 7 7 11 11 12 12 15 15 * 26 26 * 29 29 29 29 * 29 29 * 29 29 * 31 31 * 33 33 * 31 31 16 17 17 * 33 33 16 18 18 20 20 21 21 * 1
