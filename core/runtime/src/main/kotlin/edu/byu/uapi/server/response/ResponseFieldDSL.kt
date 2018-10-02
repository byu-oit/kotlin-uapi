package edu.byu.uapi.server.response

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

inline fun <UserContext : Any, Model : Any> uapiResponse(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
    : List<ResponseFieldDefinition<UserContext, Model, *>> {
    val r = UAPIResponseInit<UserContext, Model>()
    r.fn()
    return r.getList()
}

class UAPIResponseInit<UserContext : Any, Model : Any>() {

    @PublishedApi
    internal val fieldList: MutableList<ResponseFieldDefinition<UserContext, Model, *>> = mutableListOf()

    inline fun <reified T : Any> value(
        name: String,
        fn: UAPIValueInit<UserContext, Model, T>.() -> Unit
    ) {
        val p = UAPIValueInit<UserContext, Model, T>(name, T::class)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified T : Any> value(
        prop: KProperty1<Model, T>,
        fn: ValueInit<UserContext, Model, T, T>.() -> Unit
    ) {
        //TODO normalize name
        val p = UAPIValueInit<UserContext, Model, T>(prop.name, T::class)
        p.getValue(prop) //TODO: We might want to make a separate type for property-driven values
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified T : Any> nullableValue(
        name: String,
        fn: ValueInit<UserContext, Model, T?, T>.() -> Unit
    ) {
        val p = NullableUAPIValueInit<UserContext, Model, T>(name, T::class)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified T : Any> nullableValue(
        prop: KProperty1<Model, T?>,
        fn: ValueInit<UserContext, Model, T?, T>.() -> Unit
    ) {
        //TODO normalize name
        val p = NullableUAPIValueInit<UserContext, Model, T>(prop.name, T::class)
        p.getValue(prop) //TODO: We might want to make a separate type for property-driven values
        p.fn()
        fieldList.add(p.toDefinition())
    }

    fun getList(): List<ResponseFieldDefinition<UserContext, Model, *>> = fieldList

}

sealed class ValueInit<UserContext : Any, Model : Any, Type, NotNullType : Any> {
    protected lateinit var valueGetter: ValuePropGetter<Model, Type>

    val nullable: Boolean = false

    fun getValue(fn: ValuePropGetter<Model, Type>) {
        valueGetter = fn
    }

    var key: Boolean = false

    protected var descriptionFn: ValuePropDescriber<Model, NotNullType>? = null

    fun description(fn: ValuePropDescriber<Model, NotNullType>) {
        this.descriptionFn = fn
    }

    protected var longDescriptionFn: ValuePropDescriber<Model, NotNullType>? = null

    fun longDescription(fn: ValuePropDescriber<Model, NotNullType>) {
        longDescriptionFn = fn
    }

    protected var modifiableFn: ValuePropModifiable<UserContext, Model, Type>? = null

    fun modifiable(fn: ValuePropModifiable<UserContext, Model, Type>) {
        this.modifiableFn = fn
    }

    var isSystem: Boolean = false
    var isDerived: Boolean = false
    var doc: String? = null
    var displayLabel: String? = null

    @PublishedApi
    internal abstract fun toDefinition(): ResponseFieldDefinition<UserContext, Model, *>

}

class UAPIValueInit<UserContext : Any, Model : Any, Type : Any>(
    val name: String,
    val type: KClass<Type>
) : ValueInit<UserContext, Model, Type, Type>() {

    @PublishedApi
    override fun toDefinition(): ResponseFieldDefinition<UserContext, Model, *> {
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

class NullableUAPIValueInit<UserContext : Any, Model : Any, Type : Any>(
    val name: String,
    val type: KClass<Type>
) : ValueInit<UserContext, Model, Type?, Type>() {

    @PublishedApi
    override fun toDefinition(): ResponseFieldDefinition<UserContext, Model, *> {
        return NullableValueResponseFieldDefinition(
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
