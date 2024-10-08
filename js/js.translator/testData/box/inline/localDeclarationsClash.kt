// FILE: 1.kt

package o

import I

inline fun run(): String {
    return object : I {
        override fun run() = "O"
    }.run()
}


// FILE: 2.kt

package k

import I

inline fun run(): String {
    return object : I {
        override fun run() = "K"
    }.run()
}

// FILE: 3.kt

// CHECK_BREAKS_COUNT: function=ok count=0
// CHECK_LABELS_COUNT: function=ok name=$l$block count=0
fun ok() = o.run() + k.run()

// FILE: main.kt
// RECOMPILE
interface I {
    fun run(): String
}

fun box(): String {

    if (ok() != "OK") return "fail"


    return o.run() + k.run()
}