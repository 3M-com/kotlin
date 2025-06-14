// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// CHECK_TYPE
open class Outer<E> {
    inner open class Inner<F> {
        inner class Inner2<D> {

        }
    }
}

class DerivedOuter : Outer<String>() {
    inner class DerivedInner : Inner<Int>() {
        fun foo(): Inner2<Char> = null!!
    }
}

fun foo() {
    DerivedOuter().DerivedInner().foo() checkType { _<Outer<String>.Inner<Int>.Inner2<Char>>() }
}

/* GENERATED_FIR_TAGS: checkNotNullCall, classDeclaration, funWithExtensionReceiver, functionDeclaration, functionalType,
infix, inner, lambdaLiteral, nullableType, typeParameter, typeWithExtension */
