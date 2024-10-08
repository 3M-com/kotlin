package foo

class A(val x: Int) {
    var s = "sA:init:$x"
}

class B(val arg1: String, val arg2: String) {
    var msg = arg1 + arg2
}

fun box(): String {
    val ref = ::A
    val result = ref(1).s + (::B)("23", "45").msg
    return (if (result == "sA:init:12345") "OK" else result)
}
