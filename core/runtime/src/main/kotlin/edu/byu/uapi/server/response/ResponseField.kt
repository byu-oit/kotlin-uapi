package edu.byu.uapi.server.response

import edu.byu.uapi.server.types.*
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

}

typealias ArrayPropGetter<Model, Item> = (Model) -> Collection<Item>
typealias ArrayPropDescriber<Model, Item> = (Model, item: Item, index: Int) -> String?

sealed class ValueResponseFieldBase<UserContext : Any, Model : Any, Type, NonNullType : Any>
    : ResponseField<UserContext, Model, UAPIValueProperty<Type>>() {

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
        apiType: APIType,
        key: Boolean,
        description: OrMissing<String?>,
        longDescription: OrMissing<String?>,
        displayLabel: String?,
        domain: OrMissing<String>,
        relatedResource: OrMissing<String>
    ): UAPIValueProperty<Type>

}

class ValueArrayResponseField<UserContext : Any, Model : Any, Item : Any>(
    val itemType: KClass<Item>,
    override val name: String,
    val getValues: ArrayPropGetter<Model, Item>,
    override val key: Boolean = false,
    val description: ArrayPropDescriber<Model, Item>? = null,
    val longDescription: ArrayPropDescriber<Model, Item>? = null,
    val modifiable: ValuePropModifiable<UserContext, Model, Collection<Item>>? = null,
    override val isSystem: Boolean = false,
    override val isDerived: Boolean = false,
    override val doc: String? = null,
    override val displayLabel: String? = null
) : ResponseField<UserContext, Model, UAPIValueArrayProperty<Item>>() {
    override fun toProp(
        userContext: UserContext,
        model: Model
    ): UAPIValueArrayProperty<Item> {
        val values = this.getValues(model)

        val modifiable = this.modifiable?.invoke(userContext, model, values)

        val apiType = getApiType(modifiable, isDerived, isSystem)

        val decoratedValues = values.mapIndexed { index, item ->
            val (desc, longDesc) = getDescriptions(model, item, index)
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
        value: Item,
        index: Int
    ): Pair<OrMissing<String?>, OrMissing<String?>> {
        val desc = description.orMissing()
        val longDesc = longDescription.orMissing()

        val d = desc.map { fn -> fn(model, value, index) }
        val ld = longDesc.map { fn -> fn(model, value, index) }
        return d to ld
    }

}

fun getApiType(
    modifiable: Boolean?,
    derived: Boolean,
    system: Boolean
): APIType {
    return when {
        modifiable != null -> if (modifiable) APIType.MODIFIABLE else APIType.READ_ONLY
        derived -> APIType.DERIVED
        system -> APIType.SYSTEM
        // TODO(Handle 'related')
        else -> APIType.READ_ONLY
    }
}

class ValueResponseField<UserContext : Any, Model : Any, Type : Any>(
    val type: KClass<Type>,
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
) : ValueResponseFieldBase<UserContext, Model, Type, Type>() {

    //Oh, the joys of hacking around Java's Generic system
    override fun asNonNull(value: Type): Type = value

    override fun construct(
        value: Type,
        apiType: APIType,
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
    val type: KClass<Type>,
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
) : ValueResponseFieldBase<UserContext, Model, Type?, Type>() {

    //Oh, the joys of hacking around Java's Generic system
    @Suppress("UNCHECKED_CAST")
    override fun asNonNull(value: Type?): Type = value as Type

    override fun construct(
        value: Type?,
        apiType: APIType,
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

private inline fun <P, R1, R2> ((P) -> R1).then(crossinline fn: (R1) -> R2): (P) -> R2 {
    return { p -> fn(this(p)) }
}

private inline fun <P1, P2, R1, R2> ((P1, P2) -> R1).then(crossinline fn: (R1) -> R2): (P1, P2) -> R2 {
    return { p1, p2 -> fn(this(p1, p2)) }
}

private inline fun <P, PPrime, R> ((PPrime) -> R).before(crossinline fn: (P) -> PPrime): (P) -> R {
    return { p -> this(fn(p)) }
}

private inline fun <P1, P1Prime, P2, P2Prime, R> ((P1Prime, P2Prime) -> R).before(crossinline fn: (P1, P2) -> Pair<P1Prime, P2Prime>): (P1, P2) -> R {
    return { p1, p2 ->
        val (p1p, p2p) = fn(p1, p2)
        this(p1p, p2p)
    }
}


