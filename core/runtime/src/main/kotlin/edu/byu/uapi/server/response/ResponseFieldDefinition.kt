package edu.byu.uapi.server.response

import edu.byu.uapi.server.types.APIType
import edu.byu.uapi.server.types.OrMissing
import edu.byu.uapi.server.types.UAPIProperty
import edu.byu.uapi.server.types.UAPIValueProperty
import java.time.Instant
import java.util.*

typealias Describer<Model, Value> = (Model, Value) -> String?

typealias PropConstructor<Value, Prop> = (
    value: Value,
    apiType: APIType,
    key: Boolean,
    description: OrMissing<String>,
    longDescription: OrMissing<String>,
    displayLabel: String?,
    domain: OrMissing<String>,
    relatedResource: OrMissing<String>
) -> Prop

sealed class ResponseFieldDefinition<UserContext : Any, Model : Any, Type, Prop : UAPIProperty>(
    val name: String,
    val getValue: (Model) -> Type,
    val key: Boolean = false,
    val description: Describer<Model, Type>? = null,
    val longDescription: Describer<Model, Type>? = null,
    val modifiable: ((UserContext, Model) -> Boolean)? = null,
    val isSystem: Boolean = false,
    val isDerived: Boolean = false,
    val doc: String? = null,
    val displayLabel: String? = null
) {

    fun toProp(
        userContext: UserContext,
        model: Model
    ): Prop {
        val value = this.getValue(model)
        val getDesc = this.description
        val description = if (getDesc == null) OrMissing.Missing else OrMissing.Present(getDesc(model, value))
        val getLongDesc = this.description
        val longDescription = if (getLongDesc == null) OrMissing.Missing else OrMissing.Present(getLongDesc(model, value))

        val getModifiable = this.modifiable

        val apiType = when {
            getModifiable != null -> if (getModifiable(userContext, model)) APIType.MODIFIABLE else APIType.READ_ONLY
            isDerived -> APIType.DERIVED
            isSystem -> APIType.SYSTEM
            // TODO(Handle 'related')
            else -> APIType.READ_ONLY
        }

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

    //    protected abstract val propConstructor: PropConstructor<Type, Prop>
    protected abstract fun construct(
        value: Type,
        apiType: APIType,
        key: Boolean,
        description: OrMissing<String>,
        longDescription: OrMissing<String>,
        displayLabel: String?,
        domain: OrMissing<String>,
        relatedResource: OrMissing<String>
    ): Prop

}

class ValueResponseFieldDefinition<UserContext : Any, Model : Any, Type>(
    name: String,
    getValue: (Model) -> Type,
    key: Boolean = false,
    description: Describer<Model, Type>? = null,
    longDescription: Describer<Model, Type>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, Type, UAPIValueProperty<*>>(
    name = name,
    getValue = getValue,
    key = key,
    description = description,
    longDescription = longDescription,
    modifiable = modifiable,
    isSystem = isSystem,
    isDerived = isDerived,
    doc = doc,
    displayLabel = displayLabel
) {
    override fun construct(
        value: Type,
        apiType: APIType,
        key: Boolean,
        description: OrMissing<String>,
        longDescription: OrMissing<String>,
        displayLabel: String?,
        domain: OrMissing<String>,
        relatedResource: OrMissing<String>
    ): UAPIValueProperty<*> {
//        return UAPIValueProperty(
//
//        )
        TODO("not implemented")
    }
}

private fun Instant.toDate() = Date(this.toEpochMilli())

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


