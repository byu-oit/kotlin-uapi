package edu.byu.uapi.server.types

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZonedDateTime

sealed class UAPIProperty<Value : Any> {
    abstract val value: Value?
    abstract val apiType: APIType
    abstract val key: Boolean
    abstract val description: OrMissing<String>
    abstract val longDescription: OrMissing<String>
    abstract val displayLabel: String?
    abstract val domain: OrMissing<String>
    abstract val relatedResource: OrMissing<String>
}

enum class UAPIScalarTypes {
    STRING, INTEGER, DECIMAL, BOOLEAN, DATE, DATE_TIME
}

sealed class OrMissing<out Type : Any> {
    data class Present<out Type : Any>(
        val value: Type?
    ) : OrMissing<Type>()

    object Missing : OrMissing<Nothing>()
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
) : UAPIProperty<String>()

data class UAPINumber(
    override val value: Number?,
    override val apiType: APIType,
    override val key: Boolean = false,
    override val description: OrMissing<String> = OrMissing.Missing,
    override val longDescription: OrMissing<String> = OrMissing.Missing,
    override val displayLabel: String? = null,
    override val domain: OrMissing<String> = OrMissing.Missing,
    override val relatedResource: OrMissing<String> = OrMissing.Missing
) : UAPIProperty<Number>()

data class UAPIBoolean(
    override val value: Boolean?,
    override val apiType: APIType,
    override val key: Boolean = false,
    override val description: OrMissing<String> = OrMissing.Missing,
    override val longDescription: OrMissing<String> = OrMissing.Missing,
    override val displayLabel: String? = null,
    override val domain: OrMissing<String> = OrMissing.Missing,
    override val relatedResource: OrMissing<String> = OrMissing.Missing
) : UAPIProperty<Boolean>()

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


