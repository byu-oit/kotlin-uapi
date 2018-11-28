package edu.byu.uapi.server.response

import edu.byu.uapi.server.util.before
import edu.byu.uapi.server.util.then
import edu.byu.uapi.server.util.toSnakeCase
import java.lang.annotation.Inherited
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@DslMarker
@Inherited
annotation class InResponseFieldDsl

inline fun <UserContext : Any, Model : Any> uapiResponse(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
    : List<ResponseField<UserContext, Model, *>> {
    val r = UAPIResponseInit<UserContext, Model>()
    r.fn()
    return r.getList()
}


@InResponseFieldDsl
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
        val p = UAPIValueInit<UserContext, Model, T>(prop.name.toSnakeCase(), T::class)
        p.getValue(prop) //TODO: We might want to make a separate type for property-driven values
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified Output : Any, MappedFrom : Any> value(
        prop: KProperty1<Model, MappedFrom>,
        value: KProperty1<MappedFrom, Output>,
        name: String = prop.name.toSnakeCase() + "_" + value.name.toSnakeCase(),
        fn: MappedValueInit<UserContext, Model, MappedFrom, Output>.() -> Unit
    ) {
        val p = MappedValueInit<UserContext, Model, MappedFrom, Output>(name, Output::class, value)
        p.getValue(prop)
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
        val p = NullableUAPIValueInit<UserContext, Model, T>(prop.name.toSnakeCase(), T::class)
        p.getValue(prop) //TODO: We might want to make a separate type for property-driven values
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified Output : Any, MappedFrom : Any> nullableValue(
        prop: KProperty1<Model, MappedFrom?>,
        value: KProperty1<MappedFrom, Output?>,
        name: String = prop.name.toSnakeCase(),
        fn: NullableMappedValueInit<UserContext, Model, MappedFrom, Output>.() -> Unit
    ) {
        val p = NullableMappedValueInit<UserContext, Model, MappedFrom, Output>(name, Output::class, value)
        p.getValue(prop)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified T : Any> valueArray(
        name: String,
        fn: ArrayInit<UserContext, Model, T>.() -> Unit
    ) {
        val p = ArrayInit<UserContext, Model, T>(name, T::class)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified T : Any> valueArray(
        prop: KProperty1<Model, Collection<T>>,
        fn: ArrayInit<UserContext, Model, T>.() -> Unit
    ) {
        val p = ArrayInit<UserContext, Model, T>(prop.name.toSnakeCase(), T::class)
        p.getValues(prop)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified Value : Any, Item : Any> mappedValueArray(
        name: String,
        listProp: KProperty1<Model, Collection<Item?>>,
        valueProp: KProperty1<Item, Value>,
        fn: MappedValueArrayInit<UserContext, Model, Value, Item>.() -> Unit
    ) {
        val p = MappedValueArrayInit<UserContext, Model, Value, Item>(name.toSnakeCase(), Value::class)
        p.getArray(listProp)
        p.getValue(valueProp)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified Value : Any, Item : Any> mappedValueArray(
        listProp: KProperty1<Model, Collection<Item?>>,
        valueProp: KProperty1<Item, Value>,
        fn: MappedValueArrayInit<UserContext, Model, Value, Item>.() -> Unit
    ) {
        val p = MappedValueArrayInit<UserContext, Model, Value, Item>(listProp.name.toSnakeCase(), Value::class)
        p.getArray(listProp)
        p.getValue(valueProp)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    inline fun <reified Value : Any, Item : Any> mappedValueArray(
        name: String,
        fn: MappedValueArrayInit<UserContext, Model, Value, Item>.() -> Unit
    ) {
        val p = MappedValueArrayInit<UserContext, Model, Value, Item>(name.toSnakeCase(), Value::class)
        p.fn()
        fieldList.add(p.toDefinition())
    }

    fun getList(): List<ResponseField<UserContext, Model, *>> = fieldList

}

@InResponseFieldDsl
sealed class PropInit<UserContext : Any, Model : Any, Type> {

    protected var modifiableFn: ValuePropModifiable<UserContext, Model, Type>? = null

    fun modifiable(fn: ValuePropModifiable<UserContext, Model, Type>) {
        this.modifiableFn = fn
    }

    var key: Boolean = false
    var isSystem: Boolean = false
    var isDerived: Boolean = false
    var doc: String? = null
    var displayLabel: String? = null
}

class ArrayInit<UserContext : Any, Model : Any, Type : Any>(
    internal val name: String,
    internal val itemType: KClass<Type>
) : PropInit<UserContext, Model, Collection<Type?>>() {
    protected lateinit var valueGetter: ArrayPropGetter<Model, Type?>

    fun getValues(fn: ArrayPropGetter<Model, Type?>) {
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
        return SimpleValueArrayResponseField(
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

class MappedValueArrayInit<UserContext : Any, Model : Any, Value : Any, Item : Any>(
    internal val name: String,
    internal val itemType: KClass<Value>
) : PropInit<UserContext, Model, Collection<Value?>>() {
    protected lateinit var arrayGetter: ArrayPropGetter<Model, Item?>

    fun getArray(fn: ArrayPropGetter<Model, Item?>) {
        arrayGetter = fn
    }

    protected lateinit var valueGetter: TransformingArrayValueGetter<Model, Item, Value>

    inline fun getValue(crossinline fn: ValuePropGetter<Item, Value>) {
        this.getValue { model, item -> fn(item) }
    }

    fun getValue(fn: TransformingArrayValueGetter<Model, Item, Value>) {
        this.valueGetter = fn
    }

    protected var descriptionFn: TransformingArrayDescriber<Model, Item, Value>? = null

    fun description(fn: TransformingArrayDescriber<Model, Item, Value>) {
        this.descriptionFn = fn
    }

    inline fun description(crossinline fn: ArrayPropDescriber<Model, Value>) {
        this.description { model, _, valueArray, _, value, index -> fn(model, valueArray, value, index) }
    }

    inline fun description(crossinline fn: ValuePropDescriber<Item, Value>) {
        this.description { _, _, _, item, value, _ -> fn(item, value) }
    }

    inline fun description(crossinline fn: (Item) -> String?) {
        this.description { _, _, _, item, _, _ -> fn(item) }
    }

    protected var longDescriptionFn: TransformingArrayDescriber<Model, Item, Value>? = null

    fun longDescription(fn: TransformingArrayDescriber<Model, Item, Value>) {
        this.longDescriptionFn = fn
    }

    inline fun longDescription(crossinline fn: ArrayPropDescriber<Model, Value>) {
        this.longDescription { model, _, valueArray, _, value, index -> fn(model, valueArray, value, index) }
    }

    inline fun longDescription(crossinline fn: ValuePropDescriber<Item, Value>) {
        this.longDescription { _, _, _, item, value, _ -> fn(item, value) }
    }

    inline fun longDescription(crossinline fn: (Item) -> String?) {
        this.longDescription { _, _, _, item, _, _ -> fn(item) }
    }

    @PublishedApi
    internal fun toDefinition(): ResponseField<UserContext, Model, *> {
        return MappedValueArrayResponseField(
            itemType = itemType,
            name = name,
            getArray = arrayGetter,
            getValue = valueGetter,
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


@InResponseFieldDsl
sealed class ValueInit<UserContext : Any, Model : Any, Type, NotNullType : Any> : PropInit<UserContext, Model, Type>() {
    protected lateinit var valueGetter: ValuePropGetter<Model, Type>

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
    internal val name: String,
    internal val type: KClass<Type>
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

class MappedValueInit<UserContext : Any, Model : Any, MappedFrom : Any, Output : Any>(
    internal val name: String,
    internal val type: KClass<Output>,
    internal val getOutput: KProperty1<MappedFrom, Output>
) : ValueInit<UserContext, Model, MappedFrom, MappedFrom>() {

    inline fun description(crossinline fn: (MappedFrom) -> String?) {
        super.description { _: Model, value: MappedFrom -> fn(value) }
    }

    inline fun longDescription(crossinline fn: (MappedFrom) -> String?) {
        super.longDescription { _: Model, value: MappedFrom -> fn(value) }
    }

    @PublishedApi
    override fun toDefinition(): ResponseField<UserContext, Model, *> {
        return ValueResponseField(
            type,
            name,
            valueGetter.then(getOutput),
            key,
            descriptionFn?.before { model, value -> model to valueGetter(model) },
            longDescriptionFn?.before { model, value -> model to valueGetter(model) },
            modifiableFn?.before { userContext, model, value -> Triple(userContext, model, valueGetter(model)) },
            isSystem, isDerived, doc,
            displayLabel
        )
    }
}


class NullableUAPIValueInit<UserContext : Any, Model : Any, Type : Any>(
    internal val name: String,
    internal val type: KClass<Type>
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

class NullableMappedValueInit<UserContext : Any, Model : Any, MappedFrom : Any, Output : Any>(
    internal val name: String,
    internal val type: KClass<Output>,
    internal val getOutput: KProperty1<MappedFrom, Output?>
) : ValueInit<UserContext, Model, MappedFrom?, MappedFrom>() {

    inline fun description(crossinline fn: (MappedFrom) -> String?) {
        super.description { _: Model, value: MappedFrom -> fn(value) }
    }

    inline fun longDescription(crossinline fn: (MappedFrom) -> String?) {
        super.longDescription { _: Model, value: MappedFrom -> fn(value) }
    }

    @PublishedApi
    override fun toDefinition(): ResponseField<UserContext, Model, *> {
        return NullableValueResponseField(
            type,
            name,
            valueGetter.then { it?.run { getOutput(this) } },
            key,
            descriptionFn?.mapped(),
            longDescriptionFn?.mapped(),
            modifiableFn?.before { userContext, model, value -> Triple(userContext, model, valueGetter(model)) },
            isSystem, isDerived, doc,
            displayLabel
        )
    }

    private fun ValuePropDescriber<Model, MappedFrom>.mapped(): ValuePropDescriber<Model, Output> {
        return { model, output ->
            valueGetter(model)?.let { this(model, it) }
        }
    }
}
