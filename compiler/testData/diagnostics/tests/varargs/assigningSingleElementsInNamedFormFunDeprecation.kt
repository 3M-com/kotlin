// RUN_PIPELINE_TILL: FRONTEND
// LANGUAGE: +AssigningArraysToVarargsInNamedFormInAnnotations, +ProhibitAssigningSingleElementsToVarargsInNamedForm
// DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

fun foo(vararg s: Int) {}

open class Cls(vararg p: Long)

fun test(i: IntArray) {
    foo(s = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)
    foo(s = i)
    foo(s = *<!REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_FUNCTION!>i<!>)
    foo(s = intArrayOf(1))
    foo(s = *<!REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_FUNCTION!>intArrayOf(1)<!>)
    foo(1)

    Cls(p = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)

    class Sub : Cls(p = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)

    val c = object : Cls(p = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>) {}

    foo(s = *<!REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_FUNCTION!>intArrayOf(elements = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)<!>)
}


fun anyFoo(vararg a: Any) {}

fun testAny() {
    anyFoo(a = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, TYPE_MISMATCH!>""<!>)
    anyFoo(a = arrayOf(""))
    anyFoo(a = *<!REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_FUNCTION!>arrayOf("")<!>)
}

fun <T> genFoo(vararg t: T) {}

fun testGen() {
    genFoo<Int>(t = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, CONSTANT_EXPECTED_TYPE_MISMATCH!>1<!>)
    genFoo<Int?>(t = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, NULL_FOR_NONNULL_TYPE!>null<!>)
    genFoo<Array<Int>>(t = arrayOf())
    genFoo<Array<Int>>(t = *<!REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_FUNCTION!>arrayOf(arrayOf())<!>)

    genFoo(t = <!ASSIGNING_SINGLE_ELEMENT_TO_VARARG_IN_NAMED_FORM_FUNCTION_ERROR, TYPE_MISMATCH!>""<!>)
    genFoo(t = arrayOf(""))
    genFoo(t = *<!REDUNDANT_SPREAD_OPERATOR_IN_NAMED_FORM_IN_FUNCTION!>arrayOf("")<!>)
}

fun manyFoo(vararg v: Int) {}
fun manyFoo(vararg s: String) {}

fun testMany(a: Any) {
    <!NONE_APPLICABLE!>manyFoo<!>(<!DEBUG_INFO_MISSING_UNRESOLVED!>v<!> = 1)
    <!NONE_APPLICABLE!>manyFoo<!>(<!DEBUG_INFO_MISSING_UNRESOLVED!>s<!> = "")

    <!NONE_APPLICABLE!>manyFoo<!>(a)
    <!NONE_APPLICABLE!>manyFoo<!>(<!DEBUG_INFO_MISSING_UNRESOLVED!>v<!> = a)
    <!NONE_APPLICABLE!>manyFoo<!>(<!DEBUG_INFO_MISSING_UNRESOLVED!>s<!> = a)
    <!NONE_APPLICABLE!>manyFoo<!>(<!DEBUG_INFO_MISSING_UNRESOLVED!>v<!> = a as Int)
    <!NONE_APPLICABLE!>manyFoo<!>(<!DEBUG_INFO_MISSING_UNRESOLVED!>s<!> = a as String)
}

/* GENERATED_FIR_TAGS: anonymousObjectExpression, asExpression, classDeclaration, functionDeclaration, integerLiteral,
localClass, localProperty, nullableType, primaryConstructor, propertyDeclaration, smartcast, stringLiteral,
typeParameter, vararg */
