// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
interface D {
    fun foo()
}

interface E {
    fun foo() {}
}

object Impl : D, E {
    override fun foo() {}
}

val obj: D = <!DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE, MANY_IMPL_MEMBER_NOT_IMPLEMENTED!>object<!> : D by Impl, E by Impl {}

/* GENERATED_FIR_TAGS: anonymousObjectExpression, functionDeclaration, inheritanceDelegation, interfaceDeclaration,
objectDeclaration, override, propertyDeclaration */
