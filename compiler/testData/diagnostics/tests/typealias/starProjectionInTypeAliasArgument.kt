// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
class NumCharSeq<N : Number, M : CharSequence>(val n: N, val m: M)

typealias Test<X, Y> = NumCharSeq<X, Y>

fun getN(t: Test<*, *>) = t.n
fun getM(t: Test<*, *>) = t.m
