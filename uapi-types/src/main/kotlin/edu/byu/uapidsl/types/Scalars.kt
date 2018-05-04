package edu.byu.uapidsl.types

import java.math.BigDecimal
import java.net.URI
import java.time.*
import kotlin.reflect.KClass

sealed class UAPIScalar<Type>(
    val value: Type?
)

class UAPIBoolean(value: Boolean?): UAPIScalar<Boolean>(value)

class UAPIInt(value: Int?): UAPIScalar<Int>(value)

class UAPILong(value: Long?): UAPIScalar<Long>(value)

class UAPIDecimal(value: BigDecimal?): UAPIScalar<BigDecimal>(value) {
    constructor(float: Float?): this(float?.toBigDecimal())
    constructor(double: Double?): this(double?.toBigDecimal())
}

class UAPIString(value: String?): UAPIScalar<String>(value)

class UAPIByteArray(value: ByteArray?): UAPIScalar<ByteArray>(value)

class UAPIDate(value: LocalDate?): UAPIScalar<LocalDate>(value)

class UAPITime(value: LocalTime?): UAPIScalar<LocalTime>(value)

class UAPIDateTime(value: OffsetDateTime?): UAPIScalar<OffsetDateTime>(value) {
    constructor(value: ZonedDateTime?): this(value?.toOffsetDateTime())
    constructor(value: Instant?): this(value?.atZone(ZoneId.systemDefault()))
}

class UAPIUri(value: URI?): UAPIScalar<String>(value?.toString())

data class JsonSchemaType(val type: String, val format: String? = null)

val schemaTypes = mapOf<KClass<out UAPIScalar<*>>, JsonSchemaType>(
    UAPIInt::class to JsonSchemaType("integer", "int32"),
    UAPILong::class to JsonSchemaType("integer", "int64"),
    UAPIDecimal::class to JsonSchemaType("string", "decimal"),
    UAPIString::class to JsonSchemaType("string"),
    UAPIByteArray::class to JsonSchemaType("string", "byte"),
    UAPIBoolean::class to JsonSchemaType("boolean"),
    UAPIDate::class to JsonSchemaType("string", "date"),
    UAPITime::class to JsonSchemaType("string", "time"),
    UAPIDateTime::class to JsonSchemaType("string", "date-time"),
    UAPIUri::class to JsonSchemaType("string", "uri")
)

