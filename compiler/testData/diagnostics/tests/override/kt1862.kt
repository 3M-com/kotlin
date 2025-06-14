// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
open class Aaa() {
    open fun foo() = 1
}

open class Bbb() : Aaa() {
    override fun foo() = 2
}

interface Ccc : <!INTERFACE_WITH_SUPERCLASS!>Aaa<!>

class Ddd() : Bbb(), Ccc

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, integerLiteral, interfaceDeclaration, override,
primaryConstructor */
