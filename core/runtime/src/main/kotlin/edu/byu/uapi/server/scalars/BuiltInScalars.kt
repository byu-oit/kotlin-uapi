package edu.byu.uapi.server.scalars

import edu.byu.uapi.server.inputs.typeFailure
import edu.byu.uapi.server.types.APIType
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.functional.asSuccess
import edu.byu.uapi.spi.rendering.ScalarRenderer
import edu.byu.uapi.spi.scalars.ScalarFormat
import edu.byu.uapi.spi.scalars.ScalarType
import java.math.BigDecimal
import java.math.BigInteger
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.ByteBuffer
import java.time.*
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.reflect.KClass

val builtinScalarTypes: List<ScalarType<*>> = listOf<ScalarType<*>>(
    // Primitives and pseudo-primitives
    StringScalarType,
    BooleanScalarType,
    CharScalarType,
    ByteScalarType,
    ShortScalarType,
    IntScalarType,
    FloatScalarType,
    LongScalarType,
    DoubleScalarType,
    BigIntegerScalarType,
    BigDecimalScalarType,

    // Date/time
    InstantScalarType,
    LocalDateScalarType,
    LocalDateTimeScalarType,
    ZonedDateTimeScalarType,
    OffsetDateTimeScalarType,
    OffsetTimeScalarType,
    LocalTimeScalarType,
    YearMonthScalarType,
    MonthDayScalarType,
    DurationScalarType,
    PeriodScalarType,
    YearScalarType,
    EnumScalarType(DayOfWeek::class),
    EnumScalarType(Month::class),

    JavaUtilDateScalarType,
    JavaSqlDateScalarType,
    JavaSqlTimestampScalarType,

    // Misc platform types
    UUIDScalarType,
    URLScalarType,
    URIScalarType,
    ByteArrayScalarType,
    ByteBufferScalarType,

    // UAPI Built-ins
    ApiTypeScalarType
)

val builtinScalarTypeMap: Map<KClass<*>, ScalarType<*>> = builtinScalarTypes.map { it.type to it }.toMap()

open class EnumScalarType<E : Enum<E>>(
    override val type: KClass<E>
) : ScalarType<E> {

    @Suppress("UNCHECKED_CAST")
    constructor(constants: Array<E>) : this(constants.first()::class as KClass<E>)

    override val scalarFormat: ScalarFormat = ScalarFormat.STRING

    private val map: Map<String, E> by lazy {
        type.java.enumConstants.flatMap { e ->
            enumNameVariants(e.toString()).map { it to e }
        }.toMap()
    }

    override fun fromString(value: String): MaybeTypeFailure<E> {
        return map[value]?.asSuccess()
            ?: typeFailure(type, "Invalid " + type.simpleName + " value")
    }

    override fun <S> render(
        value: E,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.name)
}

object ApiTypeScalarType: EnumScalarType<APIType>(APIType::class) {
    override fun <S> render(
        value: APIType,
        renderer: ScalarRenderer<S>
    ): S {
        return renderer.string(value.apiValue)
    }
}

private fun isCamelCase(value: String): Boolean {
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

object StringScalarType : ScalarType<String> {
    override val type = String::class
    override val scalarFormat: ScalarFormat = ScalarFormat.STRING
    override fun fromString(value: String) = value.asSuccess()
    override fun <S> render(
        value: String,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value)
}

object BooleanScalarType : ScalarType<Boolean> {
    override val type = Boolean::class
    override val scalarFormat: ScalarFormat = ScalarFormat.BOOLEAN
    override fun fromString(value: String): MaybeTypeFailure<Boolean> {
        return when (value.toLowerCase()) {
            "true" -> true.asSuccess()
            "false" -> false.asSuccess()
            else -> typeFailure<Boolean>("Invalid boolean value")
        }
    }

    override fun <S> render(
        value: Boolean,
        renderer: ScalarRenderer<S>
    ) = renderer.boolean(value)
}

object CharScalarType : ScalarType<Char> {
    override val type = Char::class
    override val scalarFormat: ScalarFormat = ScalarFormat.STRING
    override fun fromString(value: String): MaybeTypeFailure<Char> {
        if (value.length != 1) {
            return typeFailure<Boolean>("Expected an input with a length of 1, got a length of ${value.length}")
        }
        return value[0].asSuccess()
    }

    override fun <S> render(
        value: Char,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object ByteScalarType : ScalarType<Byte> {
    override val type = Byte::class
    override val scalarFormat: ScalarFormat = ScalarFormat.INTEGER
    override fun fromString(value: String): MaybeTypeFailure<Byte> {
        return value.toByteOrNull()?.asSuccess() ?: typeFailure<Byte>("Invalid byte value")
    }

    override fun <S> render(
        value: Byte,
        renderer: ScalarRenderer<S>
    ) = renderer.number(value.toInt())
}

object ShortScalarType : ScalarType<Short> {
    override val type = Short::class
    override val scalarFormat: ScalarFormat = ScalarFormat.INTEGER
    override fun fromString(value: String): MaybeTypeFailure<Short> {
        return value.toShortOrNull()?.asSuccess() ?: typeFailure<Short>("Invalid short integer value")
    }

    override fun <S> render(
        value: Short,
        renderer: ScalarRenderer<S>
    ) = renderer.number(value.toInt())
}

object IntScalarType : ScalarType<Int> {
    override val type = Int::class
    override val scalarFormat: ScalarFormat = ScalarFormat.INTEGER
    override fun fromString(value: String): MaybeTypeFailure<Int> {
        return value.toIntOrNull()?.asSuccess() ?: typeFailure<Int>("Invalid integer value")
    }

    override fun <S> render(
        value: Int,
        renderer: ScalarRenderer<S>
    ) = renderer.number(value)
}

object FloatScalarType : ScalarType<Float> {
    override val type = Float::class
    override val scalarFormat: ScalarFormat = ScalarFormat.FLOAT
    override fun fromString(value: String): MaybeTypeFailure<Float> {
        return value.toFloatOrNull()?.asSuccess() ?: typeFailure<Float>("Invalid decimal value")
    }

    override fun <S> render(
        value: Float,
        renderer: ScalarRenderer<S>
    ) = renderer.number(value)
}

object LongScalarType : ScalarType<Long> {
    override val type = Long::class
    override val scalarFormat: ScalarFormat = ScalarFormat.LONG
    override fun fromString(value: String): MaybeTypeFailure<Long> {
        return value.toLongOrNull()?.asSuccess() ?: typeFailure<Long>("Invalid long integer value")
    }

    override fun <S> render(
        value: Long,
        renderer: ScalarRenderer<S>
    ) = renderer.number(value)
}

object DoubleScalarType : ScalarType<Double> {
    override val type = Double::class
    override val scalarFormat: ScalarFormat = ScalarFormat.DOUBLE
    override fun fromString(value: String): MaybeTypeFailure<Double> {
        return value.toDoubleOrNull()?.asSuccess() ?: typeFailure<Double>("Invalid long decimal value")
    }

    override fun <S> render(
        value: Double,
        renderer: ScalarRenderer<S>
    ) = renderer.number(value)
}

object BigIntegerScalarType : ScalarType<BigInteger> {
    override val type = BigInteger::class
    override val scalarFormat: ScalarFormat = ScalarFormat.LONG
    override fun fromString(value: String): MaybeTypeFailure<BigInteger> {
        return value.toBigIntegerOrNull()?.asSuccess() ?: typeFailure<BigInteger>("Invalid integer")
    }

    override fun <S> render(
        value: BigInteger,
        renderer: ScalarRenderer<S>
    ) = renderer.number(value)
}

object BigDecimalScalarType : ScalarType<BigDecimal> {
    override val type = BigDecimal::class
    override val scalarFormat: ScalarFormat = ScalarFormat.DOUBLE
    override fun fromString(value: String): MaybeTypeFailure<BigDecimal> {
        return value.toBigDecimalOrNull()?.asSuccess() ?: typeFailure<BigDecimal>("Invalid decimal")
    }

    override fun <S> render(
        value: BigDecimal,
        renderer: ScalarRenderer<S>
    ) = renderer.number(value)
}

object InstantScalarType : ScalarType<Instant> {
    override val type = Instant::class
    override val scalarFormat: ScalarFormat = ScalarFormat.DATE_TIME
    override fun fromString(value: String): MaybeTypeFailure<Instant> {
        return try {
            Instant.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<Instant>("Invalid timestamp. Must be a valid RFC-3339 'date-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun <S> render(
        value: Instant,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object LocalDateScalarType : ScalarType<LocalDate> {
    override val type = LocalDate::class
    override val scalarFormat: ScalarFormat = ScalarFormat.DATE
    override fun fromString(value: String): MaybeTypeFailure<LocalDate> {
        return try {
            LocalDate.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<LocalDate>("Invalid timestamp. Must be a valid RFC-3339 'full-date' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun <S> render(
        value: LocalDate,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object LocalDateTimeScalarType : ScalarType<LocalDateTime> {
    override val type = LocalDateTime::class
    override val scalarFormat: ScalarFormat = ScalarFormat.DATE_TIME
    override fun fromString(value: String): MaybeTypeFailure<LocalDateTime> {
        return try {
            LocalDateTime.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<LocalDateTime>("Invalid date/time. Must be a valid RFC-3339 'date-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun <S> render(
        value: LocalDateTime,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object ZonedDateTimeScalarType : ScalarType<ZonedDateTime> {
    override val type = ZonedDateTime::class
    override val scalarFormat: ScalarFormat = ScalarFormat.DATE_TIME
    override fun fromString(value: String): MaybeTypeFailure<ZonedDateTime> {
        return try {
            ZonedDateTime.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<ZonedDateTime>("Invalid date/time with time zone. Must be a valid RFC-3339 'date-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun <S> render(
        value: ZonedDateTime,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object OffsetDateTimeScalarType : ScalarType<OffsetDateTime> {
    override val type = OffsetDateTime::class
    override val scalarFormat: ScalarFormat = ScalarFormat.DATE_TIME
    override fun fromString(value: String): MaybeTypeFailure<OffsetDateTime> {
        return try {
            OffsetDateTime.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<OffsetDateTime>("Invalid date/time with zone offset. Must be a valid RFC-3339 'date-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun <S> render(
        value: OffsetDateTime,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object OffsetTimeScalarType : ScalarType<OffsetTime> {
    override val type = OffsetTime::class
    override val scalarFormat: ScalarFormat = ScalarFormat.TIME
    override fun fromString(value: String): MaybeTypeFailure<OffsetTime> {
        return try {
            OffsetTime.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<OffsetTime>("Invalid time with zone offset. Must be a valid RFC-3339 'full-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun <S> render(
        value: OffsetTime,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object LocalTimeScalarType : ScalarType<LocalTime> {
    override val type = LocalTime::class
    override val scalarFormat: ScalarFormat = ScalarFormat.TIME
    override fun fromString(value: String): MaybeTypeFailure<LocalTime> {
        return try {
            LocalTime.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<LocalTime>("Invalid time value. Must be a valid RFC-3339 'partial-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun <S> render(
        value: LocalTime,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object YearMonthScalarType : ScalarType<YearMonth> {
    override val type = YearMonth::class
    override val scalarFormat: ScalarFormat = ScalarFormat.STRING
    override fun fromString(value: String): MaybeTypeFailure<YearMonth> {
        return try {
            YearMonth.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<YearMonth>("Invalid year/month value. Must be formatted like 'yyyy-MM'.")
        }
    }

    override fun <S> render(
        value: YearMonth,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object MonthDayScalarType : ScalarType<MonthDay> {
    override val type = MonthDay::class
    override val scalarFormat: ScalarFormat = ScalarFormat.STRING
    override fun fromString(value: String): MaybeTypeFailure<MonthDay> {
        return try {
            MonthDay.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<MonthDay>("Invalid year/month value. Must be formatted like '--MM-dd', per ISO-8601.")
        }
    }

    override fun <S> render(
        value: MonthDay,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object DurationScalarType : ScalarType<Duration> {
    override val type = Duration::class
    override val scalarFormat: ScalarFormat = ScalarFormat.STRING
    override fun fromString(value: String): MaybeTypeFailure<Duration> {
        return try {
            Duration.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<Duration>("Invalid duration. Must be formatted as an ISO-8601 duration (PnDTnHnMn.nS).")
        }
    }

    override fun <S> render(
        value: Duration,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object PeriodScalarType : ScalarType<Period> {
    override val type = Period::class
    override val scalarFormat: ScalarFormat = ScalarFormat.STRING
    override fun fromString(value: String): MaybeTypeFailure<Period> {
        return try {
            Period.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<Period>("Invalid duration. Must be formatted as an ISO-8601 period (PnYnMnD or PnW).")
        }
    }

    override fun <S> render(
        value: Period,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object YearScalarType : ScalarType<Year> {
    override val type = Year::class
    override val scalarFormat: ScalarFormat = ScalarFormat.INTEGER
    override fun fromString(value: String): MaybeTypeFailure<Year> {
        return try {
            Year.parse(value).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure<Year>("Invalid year value.")
        }
    }

    override fun <S> render(
        value: Year,
        renderer: ScalarRenderer<S>
    ) = renderer.number(value.value)
}

object UUIDScalarType : ScalarType<UUID> {
    override val type = UUID::class
    override val scalarFormat: ScalarFormat = ScalarFormat.UUID
    override fun fromString(value: String): MaybeTypeFailure<UUID> {
        return try {
            UUID.fromString(value).asSuccess()
        } catch (ex: IllegalArgumentException) {
            typeFailure<UUID>("Invalid UUID value.")
        }
    }

    override fun <S> render(
        value: UUID,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toString())
}

object ByteArrayScalarType : ScalarType<ByteArray> {
    override val type = ByteArray::class
    override val scalarFormat: ScalarFormat = ScalarFormat.BYTE_ARRAY
    override fun fromString(value: String): MaybeTypeFailure<ByteArray> {
        val decoder = decoderFor(value)
            ?: return typeFailure<ByteArray>("Invalid base64-encoded bytes.")

        return try {
            decoder.decode(value).asSuccess()
        } catch (er: IllegalArgumentException) {
            typeFailure<ByteArray>("Invalid base64-encoded bytes.")
        }
    }

    override fun <S> render(
        value: ByteArray,
        renderer: ScalarRenderer<S>
    ): S {
        TODO("not implemented")
    }
}

object ByteBufferScalarType : ScalarType<ByteBuffer> {
    override val type = ByteBuffer::class
    override val scalarFormat: ScalarFormat = ScalarFormat.BYTE_ARRAY
    override fun fromString(value: String): MaybeTypeFailure<ByteBuffer> {
        val decoder = decoderFor(value)
            ?: return typeFailure<ByteBuffer>("Invalid base64-encoded bytes.")

        return try {
            ByteBuffer.wrap(decoder.decode(value)).asSuccess()
        } catch (er: IllegalArgumentException) {
            typeFailure<ByteBuffer>("Invalid base64-encoded bytes.")
        }
    }

    override fun <S> render(
        value: ByteBuffer,
        renderer: ScalarRenderer<S>
    ): S {
        TODO("not implemented")
    }
}

object URLScalarType : ScalarType<URL> {
    override val type: KClass<URL> = URL::class
    override val scalarFormat: ScalarFormat = ScalarFormat.URI

    override fun fromString(value: String): MaybeTypeFailure<URL> {
        return try {
            Success(URL(value))
        } catch (ex: MalformedURLException) {
            typeFailure(URL::class, "Malformed URL")
        }
    }

    override fun <S> render(
        value: URL,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toExternalForm())
}

object URIScalarType : ScalarType<URI> {
    override val type: KClass<URI> = URI::class
    override val scalarFormat: ScalarFormat = ScalarFormat.URI

    override fun fromString(value: String): MaybeTypeFailure<URI> {
        return try {
            Success(URI(value))
        } catch (ex: URISyntaxException) {
            typeFailure(URI::class, "Invalid URI")
        }
    }

    override fun <S> render(
        value: URI,
        renderer: ScalarRenderer<S>
    ) = renderer.string(value.toASCIIString())
}

abstract class PreJavaTimeScalarTypeBase<T : java.util.Date>
    : ScalarType<T> {
    abstract override val type: KClass<T>
    override val scalarFormat: ScalarFormat = ScalarFormat.DATE_TIME
    final override fun fromString(value: String): MaybeTypeFailure<T> {
        return try {
            val instant = Instant.parse(value)
            fromEpochMillis(instant.toEpochMilli()).asSuccess()
        } catch (ex: DateTimeParseException) {
            typeFailure(this.type, "Invalid timestamp. Must be a valid RFC-3339 'date-time' (https://tools.ietf.org/html/rfc3339).")
        }
    }

    override fun <S> render(
        value: T,
        renderer: ScalarRenderer<S>
    ) = renderer.string(Instant.ofEpochMilli(value.time).toString())

    protected abstract fun fromEpochMillis(time: Long): T
}

object JavaUtilDateScalarType : PreJavaTimeScalarTypeBase<java.util.Date>() {
    override val type = java.util.Date::class

    override fun fromEpochMillis(time: Long): Date = Date(time)
}

object JavaSqlDateScalarType : PreJavaTimeScalarTypeBase<java.sql.Date>() {
    override val type = java.sql.Date::class

    override fun fromEpochMillis(time: Long): java.sql.Date = java.sql.Date(time)
}

object JavaSqlTimestampScalarType : PreJavaTimeScalarTypeBase<java.sql.Timestamp>() {
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
