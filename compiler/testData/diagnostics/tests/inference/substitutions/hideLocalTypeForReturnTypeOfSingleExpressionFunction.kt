// FIR_IDENTICAL
// ISSUE: KT-51418

abstract class A<T>
fun f() = test("")

private fun <T> test(t: T) = object : A<T>() {
    fun bar(): T {
        return t
    }
}

fun main() {
    test(1).bar()
}