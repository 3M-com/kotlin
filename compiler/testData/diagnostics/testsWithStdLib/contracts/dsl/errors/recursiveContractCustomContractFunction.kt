// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// OPT_IN: kotlin.contracts.ExperimentalContracts
// LANGUAGE: +UseReturnsEffect
// Issue: KT-26386

fun myRun(block: () -> Unit) {
    block()
}

fun contract(block: () -> Unit) {
    block()
}

fun case_1(): Boolean? {
    contract { case_1() }
    return null
}

fun case_2(): Boolean? {
    contract { case_3() }
    return null
}

fun case_3(): Boolean? {
    contract { case_2() }
    return null
}

fun case4() {
    contract {
        myRun {
            val s: String
            run {
                s = "hello"
            }
            s.length
        }
    }
}

/* GENERATED_FIR_TAGS: assignment, functionDeclaration, functionalType, lambdaLiteral, localProperty, nullableType,
propertyDeclaration, stringLiteral */
