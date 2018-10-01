package edu.byu.uapi.server.scalars

import edu.byu.uapi.server.inputs.DeserializationFailure
import edu.byu.uapi.server.inputs.fail
import edu.byu.uapi.server.serialization.TreeSerializationStrategy
import edu.byu.uapi.server.serialization.ValueSerializationStrategy
import edu.byu.uapi.server.types.*
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer
import java.time.*
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier

interface ScalarConverter<T: Any> {
    val type: KClass<T>
    fun fromString(value: String): SuccessOrFailure<T, DeserializationFailure<T>>
    fun serialize(
        value: T?,
        strategy: ValueSerializationStrategy
    )
}

val defaultScalarConverters = mapOf<KClass<*>, ScalarConverter<*>>(
    // Primitives and pseudo-primitives
    String::class to StringScalarConverter,
    Boolean::class to BooleanScalarConverter,
    Char::class to CharScalarConverter,
    Byte::class to ByteScalarConverter,
    Short::class to ShortScalarConverter,
    Int::class to IntScalarConverter,
    Float::class to FloatScalarConverter,
    Long::class to LongScalarConverter,
    Double::class to DoubleScalarConverter,
    BigInteger::class to BigIntegerScalarConverter,
    BigDecimal::class to BigDecimalScalarConverter,

    // Date/time
    Instant::class to InstantScalarConverter,
    LocalDate::class to LocalDateScalarConverter,
    LocalDateTime::class to LocalDateTimeScalarConverter,
    ZonedDateTime::class to ZonedDateTimeScalarConverter,
    OffsetDateTime::class to OffsetDateTimeScalarConverter,
    OffsetTime::class to OffsetTimeScalarConverter,
    LocalTime::class to LocalTimeScalarConverter,
    YearMonth::class to YearMonthScalarConverter,
    MonthDay::class to MonthDayScalarConverter,
    Duration::class to DurationScalarConverter,
    Period::class to PeriodScalarConverter,
    Year::class to YearScalarConverter,
    DayOfWeek::class to EnumScalarConverter(DayOfWeek::class),
    Month::class to EnumScalarConverter(Month::class),

    java.util.Date::class to JavaUtilDateScalarConverter,
    java.sql.Date::class to JavaSqlDateScalarConverter,
    java.sql.Timestamp::class to JavaSqlTimestampScalarConverter,

    // Misc platform types
    UUID::class to UUIDScalarConverter,
    ByteArray::class to ByteArrayScalarConverter,
    ByteBuffer::class to ByteBufferScalarConverter,

    // UAPI Built-ins
    APIType::class to EnumScalarConverter(APIType::class)
)

class EnumScalarConverter<E : Enum<E>>(
    override val type: KClass<E>
) : ScalarConverter<E> {

    @Suppress("UNCHECKED_CAST")
    constructor(constants: Array<E>): this(constants.first()::class as KClass<E>)

    private val map: Map<String, E> by lazy {
        type.java.enumConstants.flatMap { e ->
            enumNameVariants(e.toString()).map { it to e }
        }.toMap()
    }

    override fun fromString(value: String): SuccessOrFailure<E, DeserializationFailure<E>> {
        return map[value]?.asSuccess()
            ?: DeserializationFailure<E>(type, "Invalid " + type.simpleName + " value").asFailure()
    }

    override fun serialize(
        value: E?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.name)
    }
}

private fun isCamelCase(value: String) : Boolean {
    if (value.isBlank()) return false
    if (!value[0].isLowerCase()) return false
    if (value.contains('_') || value.contains('-')) return false
    return value.any { it.isUpperCase() }
}

private fun enumNameVariants(name: String): Iterable<String> {
    val set = mutableSetOf(name)
    val unCameled = if (isCamelCase(name)) {//Assume camel case
        name.fold("") { acc, c ->
            if (c.isUpperCase()) {
                acc + '_' + c.toLowerCase()
            } else {
                acc + c
            }
        }
    } else {
        name
    }

    val upper = unCameled.toUpperCase()
    val lower = unCameled.toLowerCase()

    set.add(upper)
    set.add(lower)
    set.add(lower.replace('_', '-'))
    set.add(upper.replace('_', '-'))

    return set
}

object StringScalarConverter : ScalarConverter<String> {
    override val type = String::class
    override fun fromString(value: String) = value.asSuccess()
    override fun serialize(
        value: String?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value)
    }
}

object BooleanScalarConverter : ScalarConverter<Boolean> {
    override val type = Boolean::class
    override fun fromString(value: String): SuccessOrFailure<Boolean, DeserializationFailure<Boolean>> {
        return when (value.toLowerCase()) {
            "true" -> true.asSuccess()
            "false" -> false.asSuccess()
            else -> fail("Invalid boolean value")
        }
    }

    override fun serialize(
        value: Boolean?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.boolean(value)
    }
}

object CharScalarConverter : ScalarConverter<Char> {
    override val type = Char::class
    override fun fromString(value: String): SuccessOrFailure<Char, DeserializationFailure<Char>> {
        if (value.length != 1) {
            return fail("Expected an input with a length of 1, got a length of ${value.length}")
        }
        return value[0].asSuccess()
    }

    override fun serialize(
        value: Char?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object ByteScalarConverter : ScalarConverter<Byte> {
    override val type = Byte::class
    override fun fromString(value: String): SuccessOrFailure<Byte, DeserializationFailure<Byte>> {
        return value.toByteOrNull()?.asSuccess() ?: fail("Invalid byte value")
    }

    override fun serialize(
        value: Byte?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.number(value?.toInt())
    }
}

object ShortScalarConverter : ScalarConverter<Short> {
    override val type = Short::class
    override fun fromString(value: String): SuccessOrFailure<Short, DeserializationFailure<Short>> {
        return value.toShortOrNull()?.asSuccess() ?: fail("Invalid short integer value")
    }

    override fun serialize(
        value: Short?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.number(value?.toInt())
    }
}

object IntScalarConverter : ScalarConverter<Int> {
    override val type = Int::class
    override fun fromString(value: String): SuccessOrFailure<Int, DeserializationFailure<Int>> {
        return value.toIntOrNull()?.asSuccess() ?: fail("Invalid integer value")
    }

    override fun serialize(
        value: Int?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.number(value)
    }
}

object FloatScalarConverter : ScalarConverter<Float> {
    override val type = Float::class
    override fun fromString(value: String): SuccessOrFailure<Float, DeserializationFailure<Float>> {
        return value.toFloatOrNull()?.asSuccess() ?: fail("Invalid decimal value")
    }

    override fun serialize(
        value: Float?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.number(value)
    }
}

object LongScalarConverter : ScalarConverter<Long> {
    override val type = Long::class
    override fun fromString(value: String): SuccessOrFailure<Long, DeserializationFailure<Long>> {
        return value.toLongOrNull()?.asSuccess() ?: fail("Invalid long integer value")
    }

    override fun serialize(
        value: Long?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.number(value)
    }
}

object DoubleScalarConverter : ScalarConverter<Double> {
    override val type = Double::class
    override fun fromString(value: String): SuccessOrFailure<Double, DeserializationFailure<Double>> {
        return value.toDoubleOrNull()?.asSuccess() ?: fail("Invalid long decimal value")
    }

    override fun serialize(
        value: Double?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.number(value)
    }
}

object BigIntegerScalarConverter : ScalarConverter<BigInteger> {
    override val type = BigInteger::class
    override fun fromString(value: String): SuccessOrFailure<BigInteger, DeserializationFailure<BigInteger>> {
        return value.toBigIntegerOrNull()?.asSuccess() ?: fail("Invalid integer")
    }

    override fun serialize(
        value: BigInteger?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.number(value)
    }
}

object BigDecimalScalarConverter : ScalarConverter<BigDecimal> {
    override val type = BigDecimal::class
    override fun fromString(value: String): SuccessOrFailure<BigDecimal, DeserializationFailure<BigDecimal>> {
        return value.toBigDecimalOrNull()?.asSuccess() ?: fail("Invalid decimal")
    }

    override fun serialize(
        value: BigDecimal?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.number(value)
    }
}

object InstantScalarConverter : ScalarConverter<Instant> {
    override val type = Instant::class
    override fun fromString(value: String): SuccessOrFailure<Instant, DeserializationFailure<Instant>> {
        return try {
            Instant.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid timestamp. Must be a valid RFC-3339 'date-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun serialize(
        value: Instant?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object LocalDateScalarConverter : ScalarConverter<LocalDate> {
    override val type = LocalDate::class
    override fun fromString(value: String): SuccessOrFailure<LocalDate, DeserializationFailure<LocalDate>> {
        return try {
            LocalDate.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid timestamp. Must be a valid RFC-3339 'full-date' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun serialize(
        value: LocalDate?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object LocalDateTimeScalarConverter : ScalarConverter<LocalDateTime> {
    override val type = LocalDateTime::class
    override fun fromString(value: String): SuccessOrFailure<LocalDateTime, DeserializationFailure<LocalDateTime>> {
        return try {
            LocalDateTime.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid date/time. Must be a valid RFC-3339 'date-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun serialize(
        value: LocalDateTime?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object ZonedDateTimeScalarConverter : ScalarConverter<ZonedDateTime> {
    override val type = ZonedDateTime::class
    override fun fromString(value: String): SuccessOrFailure<ZonedDateTime, DeserializationFailure<ZonedDateTime>> {
        return try {
            ZonedDateTime.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid date/time with time zone. Must be a valid RFC-3339 'date-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun serialize(
        value: ZonedDateTime?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object OffsetDateTimeScalarConverter : ScalarConverter<OffsetDateTime> {
    override val type = OffsetDateTime::class
    override fun fromString(value: String): SuccessOrFailure<OffsetDateTime, DeserializationFailure<OffsetDateTime>> {
        return try {
            OffsetDateTime.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid date/time with zone offset. Must be a valid RFC-3339 'date-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun serialize(
        value: OffsetDateTime?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object OffsetTimeScalarConverter : ScalarConverter<OffsetTime> {
    override val type = OffsetTime::class
    override fun fromString(value: String): SuccessOrFailure<OffsetTime, DeserializationFailure<OffsetTime>> {
        return try {
            OffsetTime.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid time with zone offset. Must be a valid RFC-3339 'full-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun serialize(
        value: OffsetTime?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object LocalTimeScalarConverter : ScalarConverter<LocalTime> {
    override val type = LocalTime::class
    override fun fromString(value: String): SuccessOrFailure<LocalTime, DeserializationFailure<LocalTime>> {
        return try {
            LocalTime.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid time value. Must be a valid RFC-3339 'partial-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun serialize(
        value: LocalTime?,
        strategy: ValueSerializationStrategy
    ) {
       strategy.string(value?.toString())
    }
}

object YearMonthScalarConverter : ScalarConverter<YearMonth> {
    override val type = YearMonth::class
    override fun fromString(value: String): SuccessOrFailure<YearMonth, DeserializationFailure<YearMonth>> {
        return try {
            YearMonth.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid year/month value. Must be formatted like 'yyyy-MM'.")
        }
    }

    override fun serialize(
        value: YearMonth?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object MonthDayScalarConverter : ScalarConverter<MonthDay> {
    override val type = MonthDay::class
    override fun fromString(value: String): SuccessOrFailure<MonthDay, DeserializationFailure<MonthDay>> {
        return try {
            MonthDay.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid year/month value. Must be formatted like '--MM-dd', per ISO-8601.")
        }
    }

    override fun serialize(
        value: MonthDay?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object DurationScalarConverter : ScalarConverter<Duration> {
    override val type = Duration::class
    override fun fromString(value: String): SuccessOrFailure<Duration, DeserializationFailure<Duration>> {
        return try {
            Duration.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid duration. Must be formatted as an ISO-8601 duration (PnDTnHnMn.nS).")
        }
    }

    override fun serialize(
        value: Duration?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object PeriodScalarConverter : ScalarConverter<Period> {
    override val type = Period::class
    override fun fromString(value: String): SuccessOrFailure<Period, DeserializationFailure<Period>> {
        return try {
            Period.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid duration. Must be formatted as an ISO-8601 period (PnYnMnD or PnW).")
        }
    }

    override fun serialize(
        value: Period?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object YearScalarConverter : ScalarConverter<Year> {
    override val type = Year::class
    override fun fromString(value: String): SuccessOrFailure<Year, DeserializationFailure<Year>> {
        return try {
            Year.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail("Invalid year value.")
        }
    }

    override fun serialize(
        value: Year?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.number(value?.value)
    }
}

object UUIDScalarConverter : ScalarConverter<UUID> {
    override val type = UUID::class
    override fun fromString(value: String): SuccessOrFailure<UUID, DeserializationFailure<UUID>> {
        return try {
            UUID.fromString(value).asSuccess()
        } catch (ex: IllegalArgumentException) {
            fail("Invalid UUID value.")
        }
    }

    override fun serialize(
        value: UUID?,
        strategy: ValueSerializationStrategy
    ) {
        strategy.string(value?.toString())
    }
}

object ByteArrayScalarConverter : ScalarConverter<ByteArray> {
    override val type = ByteArray::class
    override fun fromString(value: String): SuccessOrFailure<ByteArray, DeserializationFailure<ByteArray>> {
        val decoder = decoderFor(value)
            ?: return fail("Invalid base64-encoded bytes.")

        return try {
            decoder.decode(value).asSuccess()
        } catch (er: IllegalArgumentException) {
            fail("Invalid base64-encoded bytes.")
        }
    }

    override fun serialize(
        value: ByteArray?,
        strategy: ValueSerializationStrategy
    ) {
        TODO("We ought to provide for binary serialization in the SerializationStrategy")
    }
}

object ByteBufferScalarConverter : ScalarConverter<ByteBuffer> {
    override val type = ByteBuffer::class
    override fun fromString(value: String): SuccessOrFailure<ByteBuffer, DeserializationFailure<ByteBuffer>> {
        val decoder = decoderFor(value)
            ?: return fail("Invalid base64-encoded bytes.")

        return try {
            ByteBuffer.wrap(decoder.decode(value)).asSuccess()
        } catch (er: IllegalArgumentException) {
            fail("Invalid base64-encoded bytes.")
        }
    }

    override fun serialize(
        value: ByteBuffer?,
        strategy: ValueSerializationStrategy
    ) {
        TODO("We ought to provide for binary serialization in the SerializationStrategy")
    }
}

abstract class PreJavaTimeScalarConverterBase<T: java.util.Date>
    : ScalarConverter<T> {
    abstract override val type: KClass<T>
    final override fun fromString(value: String): SuccessOrFailure<T, DeserializationFailure<T>> {
        return try {
            val instant = Instant.parse(value)
            fromEpochMillis(instant.toEpochMilli()).asSuccess()
        } catch (ex: DateTimeParseException) {
            fail(this.type, "Invalid timestamp. Must be a valid RFC-3339 'date-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun serialize(
        value: T?,
        strategy: ValueSerializationStrategy
    ) {
        val result = value?.let { Instant.ofEpochMilli(it.time).toString() }
        strategy.string(result)
    }

    protected abstract fun fromEpochMillis(time: Long): T
}

object JavaUtilDateScalarConverter: PreJavaTimeScalarConverterBase<java.util.Date>() {
    override val type = java.util.Date::class

    override fun fromEpochMillis(time: Long): Date = Date(time)
}

object JavaSqlDateScalarConverter: PreJavaTimeScalarConverterBase<java.sql.Date>() {
    override val type = java.sql.Date::class

    override fun fromEpochMillis(time: Long): java.sql.Date = java.sql.Date(time)
}

object JavaSqlTimestampScalarConverter: PreJavaTimeScalarConverterBase<java.sql.Timestamp>() {
    override val type = java.sql.Timestamp::class

    override fun fromEpochMillis(time: Long): java.sql.Timestamp = java.sql.Timestamp(time)
}

private fun decoderFor(value: String): Base64.Decoder? {
    return when {
        value.contains('+') || value.contains('/') -> Base64.getDecoder()
        value.contains('-') || value.contains('_') -> Base64.getUrlDecoder()
        value.contains('\r') || value.contains('\n') -> Base64.getMimeDecoder()
        else -> null
    }
}
