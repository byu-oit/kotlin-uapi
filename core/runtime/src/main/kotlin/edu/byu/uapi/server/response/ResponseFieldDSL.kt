package edu.byu.uapi.server.response

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

inline fun <UserContext : Any, Model : Any> uapiResponse(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
    : List<ResponseField<UserContext, Model, *>> {
    val r = UAPIResponseInit<UserContext, Model>()
    r.fn()
    return r.getList()
}

class UAPIResponseInit<UserContext : Any, Model : Any>() {

    @PublishedApi
    internal val fieldList: MutableList<ResponseField<UserContext, Model, *>> = mutableListOf()

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

    inline fun <reified T: Any> valueArray(
        name: String,
        fn: ArrayInit<UserContext, Model, T>.() -> Unit
    ) {
        val p = ArrayInit<UserContext, Model, T>(name, T::class)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified T: Any> valueArray(
        prop: KProperty1<Model, Collection<T>>,
        fn: ArrayInit<UserContext, Model, T>.() -> Unit
    ) {
        val p = ArrayInit<UserContext, Model, T>(prop.name, T::class)
        p.getValues(prop)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    fun getList(): List<ResponseField<UserContext, Model, *>> = fieldList

}

sealed class PropInit<UserContext: Any, Model: Any, Type> {
    var key: Boolean = false

    protected var modifiableFn: ValuePropModifiable<UserContext, Model, Type>? = null

    fun modifiable(fn: ValuePropModifiable<UserContext, Model, Type>) {
        this.modifiableFn = fn
    }

    var isSystem: Boolean = false
    var isDerived: Boolean = false
    var doc: String? = null
    var displayLabel: String? = null
}

class ArrayInit<UserContext: Any, Model: Any, Type: Any>(
    val name: String,
    val itemType: KClass<Type>
): PropInit<UserContext, Model, Collection<Type>>() {
    protected lateinit var valueGetter: ArrayPropGetter<Model, Type>

    fun getValues(fn: ArrayPropGetter<Model, Type>) {
        valueGetter = fn
    }

    protected var descriptionFn: ArrayPropDescriber<Model, Type>? = null

    fun description(fn: ArrayPropDescriber<Model, Type>) {
        this.descriptionFn = fn
    }

    protected var longDescriptionFn: ArrayPropDescriber<Model, Type>? = null

    fun longDescription(fn: ArrayPropDescriber<Model, Type>) {
        longDescriptionFn = fn
    }

    @PublishedApi
    internal fun toDefinition(): ResponseField<UserContext, Model, *> {
        return ValueArrayResponseField(
            itemType = itemType,
            name = name,
            getValues = valueGetter,
            key = key,
            description = descriptionFn,
            longDescription = longDescriptionFn,
            modifiable = modifiableFn,
            isSystem = isSystem,
            isDerived = isDerived,
            doc = doc,
            displayLabel = displayLabel
        )
    }

}

sealed class ValueInit<UserContext : Any, Model : Any, Type, NotNullType : Any>: PropInit<UserContext, Model, Type>() {
    protected lateinit var valueGetter: ValuePropGetter<Model, Type>

    val nullable: Boolean = false

    fun getValue(fn: ValuePropGetter<Model, Type>) {
        valueGetter = fn
    }

    protected var descriptionFn: ValuePropDescriber<Model, NotNullType>? = null

    fun description(fn: ValuePropDescriber<Model, NotNullType>) {
        this.descriptionFn = fn
    }

    protected var longDescriptionFn: ValuePropDescriber<Model, NotNullType>? = null

    fun longDescription(fn: ValuePropDescriber<Model, NotNullType>) {
        longDescriptionFn = fn
    }

    @PublishedApi
    internal abstract fun toDefinition(): ResponseField<UserContext, Model, *>

}

class UAPIValueInit<UserContext : Any, Model : Any, Type : Any>(
    val name: String,
    val type: KClass<Type>
) : ValueInit<UserContext, Model, Type, Type>() {

    @PublishedApi
    override fun toDefinition(): ResponseField<UserContext, Model, *> {
        return ValueResponseField(
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
    override fun toDefinition(): ResponseField<UserContext, Model, *> {
        return NullableValueResponseField(
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
