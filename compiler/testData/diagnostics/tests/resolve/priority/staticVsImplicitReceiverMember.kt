// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// CHECK_TYPE
// DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

// FILE: A.java
public class A {
    public static void foo() {}
}

// FILE: 1.kt

class C {
    fun foo() = this

    inner class B : A() {
        fun test() {
            foo() checkType { _<Unit>() }
        }
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, funWithExtensionReceiver, functionDeclaration, functionalType, infix, inner,
javaFunction, javaType, lambdaLiteral, nullableType, thisExpression, typeParameter, typeWithExtension */
