// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER

import kotlin.reflect.KProperty

operator fun String.provideDelegate(a: Any?, p: KProperty<*>) = this
operator fun String.getValue(a: Any?, p: KProperty<*>) = this

fun test(): String {
    val result by "OK"
    return result
}

/* GENERATED_FIR_TAGS: funWithExtensionReceiver, functionDeclaration, localProperty, nullableType, operator,
propertyDeclaration, propertyDelegate, starProjection, stringLiteral, thisExpression */
