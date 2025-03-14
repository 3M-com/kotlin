// TARGET_BACKEND: JVM_IR
// IGNORE_BACKEND_K1: JVM_IR
// ISSUE: KT-75649
// WITH_REFLECT

// FILE: J.java
public interface J {
    Object foo();
}
// FILE: 1.kt
class C(val j: J) {
    private lateinit var x: Any

    fun init() {
        x = j.foo()
        if (this::x.isInitialized) throw IllegalStateException("Initialized")
    }

    fun bar() = x
}

fun box(): String {
    val c = C(object : J { override fun foo() = null })
    c.init()
    return "OK"
}
