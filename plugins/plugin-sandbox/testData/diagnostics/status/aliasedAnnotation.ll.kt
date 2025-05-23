// LL_FIR_DIVERGENCE
// KT-75685: LLFirIdePredicateBasedProvider collects all owners lazily and independently from the compiler required annotations phase
// LL_FIR_DIVERGENCE
typealias MyTypeAlias = org.jetbrains.kotlin.plugin.sandbox.AllOpen

@MyTypeAlias
class A {
    fun foo() {

    }
}

@MyTypeAlias
class B : A() {
    override fun foo() {

    }
}
