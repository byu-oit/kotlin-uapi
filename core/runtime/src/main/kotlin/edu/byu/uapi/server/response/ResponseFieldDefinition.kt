package edu.byu.uapi.server.response

import edu.byu.uapi.server.types.*
import java.time.*
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

sealed class ResponseFieldDefinition<UserContext : Any, Model : Any, Type, Prop : UAPIProperty<*>>(
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

        return propConstructor(
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

    protected abstract val propConstructor: PropConstructor<Type, Prop>

}


class StringFieldDefinition<UserContext : Any, Model : Any>(
    name: String,
    getValue: (Model) -> String,
    key: Boolean = false,
    description: Describer<Model, String>? = null,
    longDescription: Describer<Model, String>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, String, UAPIString>(
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
    override val propConstructor: PropConstructor<String, UAPIString> = ::UAPIString
}

class NullableStringFieldDefinition<UserContext : Any, Model : Any>(
    name: String,
    getValue: (Model) -> String?,
    key: Boolean = false,
    description: Describer<Model, String?>? = null,
    longDescription: Describer<Model, String?>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, String?, UAPIString>(
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
    override val propConstructor: PropConstructor<String?, UAPIString> = ::UAPIString
}

class NumberFieldDefinition<UserContext : Any, Model : Any>(
    name: String,
    getValue: (Model) -> Number,
    key: Boolean = false,
    description: Describer<Model, Number>? = null,
    longDescription: Describer<Model, Number>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, Number, UAPINumber>(
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
    override val propConstructor: PropConstructor<Number, UAPINumber> = ::UAPINumber
}

class NullableNumberFieldDefinition<UserContext : Any, Model : Any>(
    name: String,
    getValue: (Model) -> Number?,
    key: Boolean = false,
    description: Describer<Model, Number?>? = null,
    longDescription: Describer<Model, Number?>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, Number?, UAPINumber>(
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
    override val propConstructor: PropConstructor<Number?, UAPINumber> = ::UAPINumber
}

class BooleanFieldDefinition<UserContext : Any, Model : Any>(
    name: String,
    getValue: (Model) -> Boolean,
    key: Boolean = false,
    description: Describer<Model, Boolean>? = null,
    longDescription: Describer<Model, Boolean>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, Boolean, UAPIBoolean>(
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
    override val propConstructor: PropConstructor<Boolean, UAPIBoolean> = ::UAPIBoolean
}

class NullableBooleanFieldDefinition<UserContext : Any, Model : Any>(
    name: String,
    getValue: (Model) -> Boolean?,
    key: Boolean = false,
    description: Describer<Model, Boolean?>? = null,
    longDescription: Describer<Model, Boolean?>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, Boolean?, UAPIBoolean>(
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
    override val propConstructor: PropConstructor<Boolean?, UAPIBoolean> = ::UAPIBoolean
}

class DateFieldDefinition<UserContext : Any, Model : Any>(
    name: String,
    getValue: (Model) -> LocalDate,
    key: Boolean = false,
    description: Describer<Model, LocalDate>? = null,
    longDescription: Describer<Model, LocalDate>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, LocalDate, UAPIDate>(
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
    override val propConstructor: PropConstructor<LocalDate, UAPIDate> = ::UAPIDate
}

class NullableDateFieldDefinition<UserContext : Any, Model : Any>(
    name: String,
    getValue: (Model) -> LocalDate?,
    key: Boolean = false,
    description: Describer<Model, LocalDate?>? = null,
    longDescription: Describer<Model, LocalDate?>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, LocalDate?, UAPIDate>(
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
    override val propConstructor: PropConstructor<LocalDate?, UAPIDate> = ::UAPIDate
}

class DateTimeFieldDefinition<UserContext : Any, Model : Any>(
    name: String,
    getValue: (Model) -> Instant,
    key: Boolean = false,
    description: Describer<Model, Instant>? = null,
    longDescription: Describer<Model, Instant>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, Instant, UAPIDateTime>(
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
    override val propConstructor: PropConstructor<Instant, UAPIDateTime> = ::UAPIDateTime

    companion object {
        fun <UserContext : Any, Model : Any> fromZonedDateTime(
            name: String,
            getValue: (Model) -> ZonedDateTime,
            key: Boolean = false,
            description: Describer<Model, ZonedDateTime>? = null,
            longDescription: Describer<Model, ZonedDateTime>? = null,
            modifiable: ((UserContext, Model) -> Boolean)? = null,
            isSystem: Boolean = false,
            isDerived: Boolean = false,
            doc: String? = null,
            displayLabel: String? = null
        ) = DateTimeFieldDefinition(
            name = name,
            getValue = getValue.then { it.toInstant() },
            key = key,
            description = description?.before { m, i -> m to i.atZone(ZoneId.systemDefault()) },
            longDescription = longDescription?.before { m, i -> m to i.atZone(ZoneId.systemDefault()) },
            modifiable = modifiable,
            isSystem = isSystem,
            isDerived = isDerived,
            doc = doc,
            displayLabel = displayLabel
        )

        fun <UserContext : Any, Model : Any> fromOffsetDateTime(
            name: String,
            getValue: (Model) -> OffsetDateTime,
            key: Boolean = false,
            description: Describer<Model, OffsetDateTime>? = null,
            longDescription: Describer<Model, OffsetDateTime>? = null,
            modifiable: ((UserContext, Model) -> Boolean)? = null,
            isSystem: Boolean = false,
            isDerived: Boolean = false,
            doc: String? = null,
            displayLabel: String? = null
        ) = DateTimeFieldDefinition(
            name = name,
            getValue = getValue.then { it.toInstant() },
            key = key,
            description = description?.before { m, i -> m to i.atOffset(ZoneOffset.UTC) },
            longDescription = longDescription?.before { m, i -> m to i.atOffset(ZoneOffset.UTC) },
            modifiable = modifiable,
            isSystem = isSystem,
            isDerived = isDerived,
            doc = doc,
            displayLabel = displayLabel
        )

        fun <UserContext : Any, Model : Any> fromDate(
            name: String,
            getValue: (Model) -> Date,
            key: Boolean = false,
            description: Describer<Model, Date>? = null,
            longDescription: Describer<Model, Date>? = null,
            modifiable: ((UserContext, Model) -> Boolean)? = null,
            isSystem: Boolean = false,
            isDerived: Boolean = false,
            doc: String? = null,
            displayLabel: String? = null
        ) = DateTimeFieldDefinition(
            name = name,
            getValue = getValue.then { it.toInstant() },
            key = key,
            description = description?.before { m, i -> m to i.toDate() },
            longDescription = longDescription?.before { m, i -> m to i.toDate() },
            modifiable = modifiable,
            isSystem = isSystem,
            isDerived = isDerived,
            doc = doc,
            displayLabel = displayLabel
        )

    }
}

class NullableDateTimeFieldDefinition<UserContext : Any, Model : Any>(
    name: String,
    getValue: (Model) -> Instant?,
    key: Boolean = false,
    description: Describer<Model, Instant?>? = null,
    longDescription: Describer<Model, Instant?>? = null,
    modifiable: ((UserContext, Model) -> Boolean)? = null,
    isSystem: Boolean = false,
    isDerived: Boolean = false,
    doc: String? = null,
    displayLabel: String? = null
) : ResponseFieldDefinition<UserContext, Model, Instant?, UAPIDateTime>(
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

    override val propConstructor: PropConstructor<Instant?, UAPIDateTime> = ::UAPIDateTime

    companion object {
        fun <UserContext : Any, Model : Any> fromZonedDateTime(
            name: String,
            getValue: (Model) -> ZonedDateTime?,
            key: Boolean = false,
            description: Describer<Model, ZonedDateTime?>? = null,
            longDescription: Describer<Model, ZonedDateTime?>? = null,
            modifiable: ((UserContext, Model) -> Boolean)? = null,
            isSystem: Boolean = false,
            isDerived: Boolean = false,
            doc: String? = null,
            displayLabel: String? = null
        ) = NullableDateTimeFieldDefinition(
            name = name,
            getValue = getValue.then { it?.toInstant() },
            key = key,
            description = description?.before { m, i -> m to i?.atZone(ZoneId.systemDefault()) },
            longDescription = longDescription?.before { m, i -> m to i?.atZone(ZoneId.systemDefault()) },
            modifiable = modifiable,
            isSystem = isSystem,
            isDerived = isDerived,
            doc = doc,
            displayLabel = displayLabel
        )

        fun <UserContext : Any, Model : Any> fromOffsetDateTime(
            name: String,
            getValue: (Model) -> OffsetDateTime?,
            key: Boolean = false,
            description: Describer<Model, OffsetDateTime?>? = null,
            longDescription: Describer<Model, OffsetDateTime?>? = null,
            modifiable: ((UserContext, Model) -> Boolean)? = null,
            isSystem: Boolean = false,
            isDerived: Boolean = false,
            doc: String? = null,
            displayLabel: String? = null
        ) = NullableDateTimeFieldDefinition(
            name = name,
            getValue = getValue.then { it?.toInstant() },
            key = key,
            description = description?.before { m, i -> m to i?.atOffset(ZoneOffset.UTC) },
            longDescription = longDescription?.before { m, i -> m to i?.atOffset(ZoneOffset.UTC) },
            modifiable = modifiable,
            isSystem = isSystem,
            isDerived = isDerived,
            doc = doc,
            displayLabel = displayLabel
        )

        fun <UserContext : Any, Model : Any> fromDate(
            name: String,
            getValue: (Model) -> Date?,
            key: Boolean = false,
            description: Describer<Model, Date?>? = null,
            longDescription: Describer<Model, Date?>? = null,
            modifiable: ((UserContext, Model) -> Boolean)? = null,
            isSystem: Boolean = false,
            isDerived: Boolean = false,
            doc: String? = null,
            displayLabel: String? = null
        ) = NullableDateTimeFieldDefinition(
            name = name,
            getValue = getValue.then { it?.toInstant() },
            key = key,
            description = description?.before { m, i -> m to i?.toDate() },
            longDescription = longDescription?.before { m, i -> m to i?.toDate() },
            modifiable = modifiable,
            isSystem = isSystem,
            isDerived = isDerived,
            doc = doc,
            displayLabel = displayLabel
        )
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


