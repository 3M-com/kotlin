// RUN_PIPELINE_TILL: FRONTEND
interface IA

interface IB : IA {
    <!METHOD_OF_ANY_IMPLEMENTED_IN_INTERFACE!>override fun toString(): String = "IB"<!>
}

interface IC : IB {
    <!METHOD_OF_ANY_IMPLEMENTED_IN_INTERFACE!>override fun toString(): String = "IC"<!>
}

interface ID : IC {
    <!METHOD_OF_ANY_IMPLEMENTED_IN_INTERFACE!>override fun toString(): String = "ID"<!>
}

/* GENERATED_FIR_TAGS: functionDeclaration, interfaceDeclaration, override, stringLiteral */
