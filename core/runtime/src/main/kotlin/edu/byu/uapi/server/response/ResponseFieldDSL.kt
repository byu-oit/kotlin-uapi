package edu.byu.uapi.server.response

import kotlin.reflect.KProperty1

inline fun <UserContext : Any, Model : Any> uapiResponse(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
    : List<ResponseFieldDefinition<UserContext, Model, *, *>> {
    val r = UAPIResponseInit<UserContext, Model>()
    r.fn()
    return emptyList()
}

class UAPIResponseInit<UserContext, Model : Any>() {
    inline fun <reified T> prop(
        name: String,
        fn: UAPIPropInit<UserContext, Model, T>.() -> Unit
    ) {
        val p = UAPIPropInit<UserContext, Model, T>(name, isNullable<T>())
        p.fn()
    }

    fun <T> prop(
        prop: KProperty1<Model, T>,
        fn: UAPIPropInit<UserContext, Model, T>.() -> Unit
    ) {
        //TODO normalize name
        val p = UAPIPropInit<UserContext, Model, T>(prop.name, prop.returnType.isMarkedNullable)
        p.fn()
    }

    inline fun <reified T> isNullable(): Boolean = null is T

}

class UAPIPropInit<UserContext, Model, Type>(
    val name: String,
    val nullable: Boolean
) {
    fun getValue(fn: (Model) -> Type) {

    }

    var key: Boolean? = null

    fun description(fn: Describer<Model, Type>) {

    }

    fun longDescription(fn: Describer<Model, Type>) {

    }

    fun modifiable(fn: (UserContext, Model) -> Boolean) {

    }

    var isSystem: Boolean? = null
    var isDerived: Boolean? = null
    var doc: String? = null
    var displayLabel: String? = null
}
