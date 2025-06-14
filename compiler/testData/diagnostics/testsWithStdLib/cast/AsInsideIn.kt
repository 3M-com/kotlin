// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// See also KT-10386
interface A
class B : A
fun foo1(list: List<A>, arg: B?): Boolean {
    // Type mismatch
    return arg <!TYPE_INFERENCE_ONLY_INPUT_TYPES_ERROR!>in<!> list // resolved to extension
}
fun foo2(list: List<A>, arg: B?): Boolean {
    // FAKE: no cast needed
    return arg as A? in list
}
fun foo3(list: List<A>, arg: B?): Boolean {
    // No warning but KNPE risk
    return arg!! in list
}
// But
fun foo4(list: List<A>, arg: B): Boolean {
    // Ok
    return arg in list
}

/* GENERATED_FIR_TAGS: asExpression, checkNotNullCall, classDeclaration, functionDeclaration, interfaceDeclaration,
nullableType */
