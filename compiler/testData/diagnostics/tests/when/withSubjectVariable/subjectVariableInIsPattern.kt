// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// LANGUAGE: +VariableDeclarationInWhenSubject
// DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

fun test(x: Any) {
    when (val y = x) {
        is String -> {}
    }
}

/* GENERATED_FIR_TAGS: functionDeclaration, isExpression, localProperty, propertyDeclaration, whenExpression,
whenWithSubject */
