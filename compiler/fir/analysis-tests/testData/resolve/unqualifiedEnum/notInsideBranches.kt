// RUN_PIPELINE_TILL: FRONTEND
// LANGUAGE: +ContextSensitiveEnumResolutionInWhen
enum class Some {
    FIRST,
    SECOND;
}

fun foo(s: Some) = when (s) {
    FIRST -> <!UNRESOLVED_REFERENCE!>SECOND<!>
    SECOND -> <!UNRESOLVED_REFERENCE!>FIRST<!>
}
