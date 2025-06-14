// RUN_PIPELINE_TILL: FRONTEND
interface One<T>
fun <E, R> foo(): E where E : Number, E : One<R>, R : One<E> = null!!

interface Entity<T>
abstract class SecuredEntity<out E>(val entity: E) where E : Entity<Int>, E : SecurityCodeAware<*,*>
interface SecurityCodeAware<out E, R : SecuredEntity<E>> where E : Entity<Int>, E : SecurityCodeAware<E, R>
fun <E, R : SecuredEntity<E>> SecurityCodeAware<E, R>.secured() : R where E : Entity<Int>, E : SecurityCodeAware<E, R> = when(this) {
    is <!INCOMPATIBLE_TYPES!>Order<!> -> <!TYPE_MISMATCH!>SecuredOrder(<!DEBUG_INFO_SMARTCAST!>this<!>)<!>
    else -> null!!
}
class Order : Entity<Int>
class SecuredOrder(order: Order) : SecuredEntity<<!UPPER_BOUND_VIOLATED!>Order<!>>(order)
fun main() {
    val securedOrder = Order().<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>secured<!>()
}

/* GENERATED_FIR_TAGS: checkNotNullCall, classDeclaration, funWithExtensionReceiver, functionDeclaration,
interfaceDeclaration, intersectionType, isExpression, localProperty, nullableType, out, primaryConstructor,
propertyDeclaration, smartcast, starProjection, thisExpression, typeConstraint, typeParameter, whenExpression,
whenWithSubject */
