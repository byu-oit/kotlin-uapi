package edu.byu.uapi.server.types

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZonedDateTime

sealed class UAPIProperty<Value : Any>: UAPISerializable {
    abstract val value: Value?
    abstract val apiType: APIType
    abstract val key: Boolean
    abstract val description: OrMissing<String>
    abstract val longDescription: OrMissing<String>
    abstract val displayLabel: String?
    abstract val domain: OrMissing<String>
    abstract val relatedResource: OrMissing<String>

    final override fun serialize(ser: SerializationStrategy) {
        serializeValue("value", value, ser)
        ser.add("api_type", apiType)
        if (key) {
            ser.add("key", key)
        }
        description.ifPresent { ser.add("description", it) }
        longDescription.ifPresent { ser.add("long_description", it) }
        displayLabel?.let { ser.add("display_label", it) }
        domain.ifPresent { ser.add("domain", it) }
        relatedResource.ifPresent { ser.add("related_resource", it) }
    }

    protected abstract fun serializeValue(key: String, value: Value?, ser: SerializationStrategy)
}

enum class UAPIScalarTypes {
    STRING, INTEGER, DECIMAL, BOOLEAN, DATE, DATE_TIME
}

sealed class OrMissing<out Type : Any> {

    abstract fun ifPresent(fn: (Type?) -> Unit)

    data class Present<out Type : Any>(
        val value: Type?
    ) : OrMissing<Type>() {
        override fun ifPresent(fn: (Type?) -> Unit) {
            fn(value)
        }
    }

    object Missing : OrMissing<Nothing>() {
        override fun ifPresent(fn: (Nothing?) -> Unit) {
        }
    }
}

data class UAPIString(
    override val value: String?,
    override val apiType: APIType,
    override val key: Boolean = false,
    override val description: OrMissing<String> = OrMissing.Missing,
    override val longDescription: OrMissing<String> = OrMissing.Missing,
    override val displayLabel: String? = null,
    override val domain: OrMissing<String> = OrMissing.Missing,
    override val relatedResource: OrMissing<String> = OrMissing.Missing
) : UAPIProperty<String>() {
    override fun serializeValue(
        key: String,
        value: String?,
        ser: SerializationStrategy
    ) {
        ser.add(key, value)
    }
}

data class UAPINumber(
    override val value: Number?,
    override val apiType: APIType,
    override val key: Boolean = false,
    override val description: OrMissing<String> = OrMissing.Missing,
    override val longDescription: OrMissing<String> = OrMissing.Missing,
    override val displayLabel: String? = null,
    override val domain: OrMissing<String> = OrMissing.Missing,
    override val relatedResource: OrMissing<String> = OrMissing.Missing
) : UAPIProperty<Number>() {
    override fun serializeValue(
        key: String,
        value: Number?,
        ser: SerializationStrategy
    ) {
        TODO("not implemented because we may split 'Number'")
    }
}

data class UAPIBoolean(
    override val value: Boolean?,
    override val apiType: APIType,
    override val key: Boolean = false,
    override val description: OrMissing<String> = OrMissing.Missing,
    override val longDescription: OrMissing<String> = OrMissing.Missing,
    override val displayLabel: String? = null,
    override val domain: OrMissing<String> = OrMissing.Missing,
    override val relatedResource: OrMissing<String> = OrMissing.Missing
) : UAPIProperty<Boolean>() {
    override fun serializeValue(
        key: String,
        value: Boolean?,
        ser: SerializationStrategy
    ) {
        ser.add(key, value)
    }
}

data class UAPIDate(
    override val value: LocalDate?,
    override val apiType: APIType,
    override val key: Boolean = false,
    override val description: OrMissing<String> = OrMissing.Missing,
    override val longDescription: OrMissing<String> = OrMissing.Missing,
    override val displayLabel: String? = null,
    override val domain: OrMissing<String> = OrMissing.Missing,
    override val relatedResource: OrMissing<String> = OrMissing.Missing
) : UAPIProperty<LocalDate>() {
    override fun serializeValue(
        key: String,
        value: LocalDate?,
        ser: SerializationStrategy
    ) {
        ser.add(key, value?.toString())
    }
}

data class UAPIDateTime(
    override val value: Instant?,
    override val apiType: APIType,
    override val key: Boolean = false,
    override val description: OrMissing<String> = OrMissing.Missing,
    override val longDescription: OrMissing<String> = OrMissing.Missing,
    override val displayLabel: String? = null,
    override val domain: OrMissing<String> = OrMissing.Missing,
    override val relatedResource: OrMissing<String> = OrMissing.Missing
) : UAPIProperty<Instant>() {
    override fun serializeValue(
        key: String,
        value: Instant?,
        ser: SerializationStrategy
    ) {
        ser.add(key, value?.toString())
    }

    constructor(
        value: ZonedDateTime?,
        apiType: APIType,
        key: Boolean = false,
        description: OrMissing<String> = OrMissing.Missing,
        longDescription: OrMissing<String> = OrMissing.Missing,
        displayLabel: String? = null,
        domain: OrMissing<String> = OrMissing.Missing,
        relatedResource: OrMissing<String> = OrMissing.Missing
    ) : this(
        value = value?.toInstant(),
        apiType = apiType,
        key = key,
        description = description,
        longDescription = longDescription,
        displayLabel = displayLabel,
        domain = domain,
        relatedResource = relatedResource
    )

    constructor(
        value: OffsetDateTime?,
        apiType: APIType,
        key: Boolean = false,
        description: OrMissing<String> = OrMissing.Missing,
        longDescription: OrMissing<String> = OrMissing.Missing,
        displayLabel: String? = null,
        domain: OrMissing<String> = OrMissing.Missing,
        relatedResource: OrMissing<String> = OrMissing.Missing
    ) : this(
        value = value?.toInstant(),
        apiType = apiType,
        key = key,
        description = description,
        longDescription = longDescription,
        displayLabel = displayLabel,
        domain = domain,
        relatedResource = relatedResource
    )
}

enum class APIType {
    READ_ONLY,
    MODIFIABLE,
    SYSTEM,
    DERIVED,
    RELATED
}


