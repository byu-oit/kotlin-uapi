package edu.byu.uapi.server.response

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure

inline fun <UserContext : Any, Model : Any> uapiResponse(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
    : List<ResponseFieldDefinition<UserContext, Model, *, *>> {
    val r = UAPIResponseInit<UserContext, Model>()
    r.fn()
    return r.getList()
}

class UAPIResponseInit<UserContext: Any, Model : Any>() {

    @PublishedApi
    internal val fieldList: MutableList<ResponseFieldDefinition<UserContext, Model, *, *>> = mutableListOf()

    inline fun <reified T: Any> prop(
        name: String,
        fn: UAPIPropInit<UserContext, Model, T>.() -> Unit
    ) {
        val p = UAPIPropInit<UserContext, Model, T>(name, T::class)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    fun <T: Any> prop(
        prop: KProperty1<Model, T>,
        fn: UAPIPropInit<UserContext, Model, T>.() -> Unit
    ) {
        //TODO normalize name
        val p = UAPIPropInit<UserContext, Model, T>(prop.name, prop.returnType.jvmErasure as KClass<T>)
        p.getValue(prop)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified T> isNullable(): Boolean = null is T

    fun getList(): List<ResponseFieldDefinition<UserContext, Model, *, *>> = fieldList

}

class UAPIPropInit<UserContext: Any, Model: Any, Type: Any>(
    val name: String,
    val type: KClass<Type>
) {
    private lateinit var valueGetter: (Model) -> Type

    fun getValue(fn: (Model) -> Type) {
        valueGetter = fn
    }

    var key: Boolean = false

    private var descriptionFn: Describer<Model, Type>? = null

    fun description(fn: Describer<Model, Type>) {
        this.descriptionFn = fn
    }

    private var longDescriptionFn: Describer<Model, Type>? = null

    fun longDescription(fn: Describer<Model, Type>) {
        longDescriptionFn = fn
    }

    private var modifiableFn: ((UserContext, Model) -> Boolean)? = null

    fun modifiable(fn: (UserContext, Model) -> Boolean) {
        this.modifiableFn = fn
    }

    var isSystem: Boolean = false
    var isDerived: Boolean = false
    var doc: String? = null
    var displayLabel: String? = null

    @PublishedApi
    internal fun toDefinition(): ResponseFieldDefinition<UserContext, Model, Type, *> {
            return ValueResponseFieldDefinition(
                type,
                name,
                valueGetter,
                key,
                descriptionFn,
                longDescriptionFn,
                modifiableFn,
                isSystem, isDerived, doc,
                displayLabel
            )
    }

}
