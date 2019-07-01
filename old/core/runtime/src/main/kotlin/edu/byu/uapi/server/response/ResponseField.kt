package edu.byu.uapi.server.response

import edu.byu.uapi.model.*
import edu.byu.uapi.server.spi.requireScalarType
import edu.byu.uapi.server.types.*
import edu.byu.uapi.spi.dictionary.TypeDictionary
import java.util.*
import kotlin.reflect.KClass

typealias ValuePropDescriber<Model, Value> = (Model, value: Value) -> String?
typealias ValuePropGetter<Model, Value> = (Model) -> Value
typealias ValuePropModifiable<UserContext, Model, Value> = (UserContext, Model, value: Value) -> Boolean

sealed class ResponseField<UserContext : Any, Model : Any, Prop : UAPIProperty> {
    abstract val name: String
    abstract val key: Boolean
    abstract val isSystem: Boolean
    abstract val isDerived: Boolean
    abstract val doc: String?
    abstract val displayLabel: String?

    abstract fun toProp(
        userContext: UserContext,
        model: Model
    ): Prop

    fun introspect(
        typeDictionary: TypeDictionary
    ): UAPIPropertyModel {
        return UAPIPropertyModel(
            type = introspectTypeModel(typeDictionary),
            apiTypes = this.possibleApiTypes
        )
    }

    abstract fun introspectTypeModel(
        typeDictionary: TypeDictionary
    ): UAPIPropertyTypeModel

    internal abstract val canBeModifiable: Boolean

    val possibleApiTypes: Set<UAPIApiType> by lazy {
        when {
            isSystem -> EnumSet.of(UAPIApiType.SYSTEM)
            isDerived -> EnumSet.of(UAPIApiType.DERIVED)
            //TODO: Handle related
            canBeModifiable -> EnumSet.of(UAPIApiType.MODIFIABLE, UAPIApiType.READ_ONLY)
            else -> EnumSet.of(UAPIApiType.READ_ONLY)
        }
    }
}

typealias ArrayPropGetter<Model, Item> = (Model) -> Collection<Item>
typealias ArrayPropDescriber<Model, Item> = (Model, array: Collection<Item?>, item: Item, index: Int) -> String?

sealed class ValueResponseField<UserContext : Any, Model : Any, Type, NonNullType : Any>
    : ResponseField<UserContext, Model, UAPIValueProperty<Type>>() {

    abstract val type: KClass<NonNullType>
    abstract override val name: String
    abstract val getValue: ValuePropGetter<Model, Type>
    abstract override val key: Boolean
    abstract val description: ValuePropDescriber<Model, NonNullType>?
    abstract val longDescription: ValuePropDescriber<Model, NonNullType>?
    abstract val modifiable: ValuePropModifiable<UserContext, Model, Type>?
    abstract override val isSystem: Boolean
    abstract override val isDerived: Boolean
    abstract override val doc: String?
    abstract override val displayLabel: String?

    abstract val nullable: Boolean

    override val canBeModifiable: Boolean by lazy { modifiable != null }

    private fun getDescriptions(
        model: Model,
        value: Type
    ): Pair<OrMissing<String?>, OrMissing<String?>> {
        val desc = description.orMissing()
        val longDesc = longDescription.orMissing()

        return if (value == null) {
            (desc.map { null } to longDesc.map { null })
        } else {
            val nonNull = asNonNull(value)
            val d = desc.map { fn -> fn(model, nonNull) }
            val ld = longDesc.map { fn -> fn(model, nonNull) }
            d to ld
        }
    }

    protected abstract fun asNonNull(value: Type): NonNullType

    override fun toProp(
        userContext: UserContext,
        model: Model
    ): UAPIValueProperty<Type> {
        val value: Type = this.getValue(model)
        val (description, longDescription) = getDescriptions(model, value)

        val modifiable = this.modifiable?.invoke(userContext, model, value)

        val apiType = getApiType(modifiable, isDerived, isSystem)

        return construct(
            value,
            apiType,
            this.key,
            description,
            longDescription,
            this.displayLabel,
            OrMissing.Missing, //TODO: domain and related resource
            OrMissing.Missing
        )
    }

    protected abstract fun construct(
        value: Type,
        apiType: UAPIApiType,
        key: Boolean,
        description: OrMissing<String?>,
        longDescription: OrMissing<String?>,
        displayLabel: String?,
        domain: OrMissing<String>,
        relatedResource: OrMissing<String>
    ): UAPIValueProperty<Type>

    override fun introspectTypeModel(typeDictionary: TypeDictionary): UAPIValuePropertyTypeModel {
        return typeDictionary.getValuePropDefinition(type)
    }
}

sealed class ValueArrayResponseField<UserContext : Any, Model : Any, Item : Any>
    : ResponseField<UserContext, Model, UAPIValueArrayProperty<Item?>>() {
    abstract val itemType: KClass<Item>

    override fun introspectTypeModel(typeDictionary: TypeDictionary): UAPIValueArrayPropertyTypeModel {
        return UAPIValueArrayPropertyTypeModel(
            items = typeDictionary.getValuePropDefinition(itemType)
        )
    }
}

class SimpleValueArrayResponseField<UserContext : Any, Model : Any, Item : Any>(
    override val itemType: KClass<Item>,
    override val name: String,
    val getValues: ArrayPropGetter<Model, Item?>,
    override val key: Boolean = false,
    val description: ArrayPropDescriber<Model, Item>? = null,
    val longDescription: ArrayPropDescriber<Model, Item>? = null,
    val modifiable: ValuePropModifiable<UserContext, Model, Collection<Item?>>? = null,
    override val isSystem: Boolean = false,
    override val isDerived: Boolean = false,
    override val doc: String? = null,
    override val displayLabel: String? = null
) : ValueArrayResponseField<UserContext, Model, Item>() {
    override val canBeModifiable: Boolean by lazy { modifiable != null }
    override fun toProp(
        userContext: UserContext,
        model: Model
    ): UAPIValueArrayProperty<Item?> {
        val values = this.getValues(model)

        val modifiable = this.modifiable?.invoke(userContext, model, values)

        val apiType = getApiType(modifiable, isDerived, isSystem)

        val decoratedValues = values.mapIndexed { index, item ->
            val (desc, longDesc) = getDescriptions(model, values, item, index)
            ValueArrayItem(
                item,
                desc,
                longDesc,
                OrMissing.Missing
            )
        }

        return UAPIValueArrayProperty(
            decoratedValues,
            apiType,
            key,
            displayLabel,
            OrMissing.Missing,
            OrMissing.Missing
        )
    }

    private fun getDescriptions(
        model: Model,
        array: Collection<Item?>,
        value: Item?,
        index: Int
    ): Pair<OrMissing<String?>, OrMissing<String?>> {
        val desc = description.orMissing()
        val longDesc = longDescription.orMissing()

        if (value == null) {
            return desc.map { null } to longDesc.map { null }
        }

        val d = desc.map { fn -> fn(model, array, value, index) }
        val ld = longDesc.map { fn -> fn(model, array, value, index) }
        return d to ld
    }

}

typealias TransformingArrayValueGetter<Model, Item, Value> = (Model, Item) -> Value
typealias TransformingArrayDescriber<Model, Item, Value> = (Model, array: Collection<Item?>, valueArray: Collection<Value?>, Item, value: Value, index: Int) -> String?
//typealias TransformingArrayModifiable<UserContext, Model, Item, Value> = (UserContext, Model, items: Collection<Item?>, values: Collection<Value?>) -> Boolean

class MappedValueArrayResponseField<UserContext : Any, Model : Any, Item : Any, Value : Any>(
    override val itemType: KClass<Value>,
    override val name: String,
    val getArray: ArrayPropGetter<Model, Item?>,
    val getValue: TransformingArrayValueGetter<Model, Item, Value>,
    override val key: Boolean = false,
    val description: TransformingArrayDescriber<Model, Item, Value>? = null,
    val longDescription: TransformingArrayDescriber<Model, Item, Value>? = null,
    val modifiable: ValuePropModifiable<UserContext, Model, Collection<Value?>>? = null,
    override val isSystem: Boolean = false,
    override val isDerived: Boolean = false,
    override val doc: String? = null,
    override val displayLabel: String? = null
) : ValueArrayResponseField<UserContext, Model, Value>() {
    override val canBeModifiable: Boolean by lazy { modifiable != null }
    override fun toProp(
        userContext: UserContext,
        model: Model
    ): UAPIValueArrayProperty<Value?> {
        val items = this.getArray(model)
        val itemAndValue = mutableListOf<Pair<Item?, Value?>>()
        val values = mutableListOf<Value?>()

        for (item in items) {
            val value = if (item == null) {
                null
            } else {
                getValue(model, item)
            }
            itemAndValue.add(item to value)
            values.add(value)
        }

        val modifiable = this.modifiable?.invoke(userContext, model, values)

        val apiType = getApiType(modifiable, isDerived, isSystem)

        val decoratedValues = itemAndValue.mapIndexed { index, pair ->
            val (item, value) = pair
            val (desc, longDesc) = getDescriptions(model, items, values, item, value, index)
            ValueArrayItem(
                value,
                desc,
                longDesc,
                OrMissing.Missing
            )
        }

        return UAPIValueArrayProperty(
            decoratedValues,
            apiType,
            key,
            displayLabel,
            OrMissing.Missing,
            OrMissing.Missing
        )
    }

    private fun getDescriptions(
        model: Model,
        items: Collection<Item?>,
        values: Collection<Value?>,
        item: Item?,
        value: Value?,
        index: Int
    ): Pair<OrMissing<String?>, OrMissing<String?>> {
        val desc = description.orMissing()
        val longDesc = longDescription.orMissing()

        if (item == null || value == null) {
            return desc.map { null } to longDesc.map { null }
        }

        val d = desc.map { fn -> fn(model, items, values, item, value, index) }
        val ld = longDesc.map { fn -> fn(model, items, values, item, value, index) }
        return d to ld
    }
}

fun getApiType(
    modifiable: Boolean?,
    derived: Boolean,
    system: Boolean
): UAPIApiType {
    return when {
        modifiable != null -> if (modifiable) UAPIApiType.MODIFIABLE else UAPIApiType.READ_ONLY
        derived -> UAPIApiType.DERIVED
        system -> UAPIApiType.SYSTEM
        // TODO(Handle 'related')
        else -> UAPIApiType.READ_ONLY
    }
}

class RequiredValueResponseField<UserContext : Any, Model : Any, Type : Any>(
    override val type: KClass<Type>,
    override val name: String,
    override val getValue: ValuePropGetter<Model, Type>,
    override val key: Boolean = false,
    override val description: ValuePropDescriber<Model, Type>? = null,
    override val longDescription: ValuePropDescriber<Model, Type>? = null,
    override val modifiable: ValuePropModifiable<UserContext, Model, Type>? = null,
    override val isSystem: Boolean = false,
    override val isDerived: Boolean = false,
    override val doc: String? = null,
    override val displayLabel: String? = null
) : ValueResponseField<UserContext, Model, Type, Type>() {

    override val nullable: Boolean = false

    //Oh, the joys of hacking around Java's Generic system
    override fun asNonNull(value: Type): Type = value

    override fun construct(
        value: Type,
        apiType: UAPIApiType,
        key: Boolean,
        description: OrMissing<String?>,
        longDescription: OrMissing<String?>,
        displayLabel: String?,
        domain: OrMissing<String>,
        relatedResource: OrMissing<String>
    ): UAPIValueProperty<Type> {
        return UAPIValueProperty(
            value,
            description,
            longDescription,
            apiType,
            key,
            displayLabel,
            domain,
            relatedResource
        )
    }
}

class NullableValueResponseField<UserContext : Any, Model : Any, Type : Any>(
    override val type: KClass<Type>,
    override val name: String,
    override val getValue: ValuePropGetter<Model, Type?>,
    override val key: Boolean = false,
    override val description: ValuePropDescriber<Model, Type>? = null,
    override val longDescription: ValuePropDescriber<Model, Type>? = null,
    override val modifiable: ValuePropModifiable<UserContext, Model, Type?>? = null,
    override val isSystem: Boolean = false,
    override val isDerived: Boolean = false,
    override val doc: String? = null,
    override val displayLabel: String? = null
) : ValueResponseField<UserContext, Model, Type?, Type>() {

    override val nullable: Boolean = true

    //Oh, the joys of hacking around Java's Generic system
    @Suppress("UNCHECKED_CAST")
    override fun asNonNull(value: Type?): Type = value as Type

    override fun construct(
        value: Type?,
        apiType: UAPIApiType,
        key: Boolean,
        description: OrMissing<String?>,
        longDescription: OrMissing<String?>,
        displayLabel: String?,
        domain: OrMissing<String>,
        relatedResource: OrMissing<String>
    ): UAPIValueProperty<Type?> {
        return UAPIValueProperty(
            value,
            description,
            longDescription,
            apiType,
            key,
            displayLabel,
            domain,
            relatedResource
        )
    }
}

internal fun TypeDictionary.getValuePropDefinition(type: KClass<*>): UAPIValuePropertyTypeModel {
    return this.requireScalarType(type).asValueDefinition()
}
