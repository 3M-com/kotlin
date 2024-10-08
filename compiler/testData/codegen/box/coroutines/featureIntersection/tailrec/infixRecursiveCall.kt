// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

tailrec suspend infix fun Int.foo(x: Int) {
    if (x == 0) return
    val xx = x - 1
    return 1 foo xx
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    builder {
        1 foo 1000000
    }
    return "OK"
}
