// RUN_PIPELINE_TILL: FRONTEND
@Target(AnnotationTarget.TYPE)
annotation class A

typealias Gen<T> = List<@A T>

typealias Test1 = Gen<<!REPEATED_ANNOTATION!>@A<!> Int>

/* GENERATED_FIR_TAGS: annotationDeclaration, nullableType, typeAliasDeclaration, typeAliasDeclarationWithTypeParameter,
typeParameter */
