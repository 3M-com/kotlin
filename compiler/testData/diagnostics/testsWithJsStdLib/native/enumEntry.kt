// DIAGNOSTICS: +ENUM_CLASS_IN_EXTERNAL_DECLARATION_WARNING
external enum class <!ENUM_CLASS_IN_EXTERNAL_DECLARATION_WARNING!>E<!> {
    X,
    Y <!EXTERNAL_ENUM_ENTRY_WITH_BODY!>{
        fun foo()
    }<!>,
    Z <!EXTERNAL_ENUM_ENTRY_WITH_BODY!>{}<!>
}