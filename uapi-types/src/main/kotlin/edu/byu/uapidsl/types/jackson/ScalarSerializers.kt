package edu.byu.uapidsl.types.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import edu.byu.uapidsl.types.*
import kotlin.reflect.KClass

internal val scalarSerializers: Map<KClass<*>, JsonSerializer<*>> =
    listOf(
        BooleanScalarSerializer,
        IntScalarSerializer,
        LongScalarSerializer,
        StringScalarSerializer(UAPIString::class),
        StringScalarSerializer(UAPIDecimal::class),
        StringScalarSerializer(UAPIByteArray::class),
        StringScalarSerializer(UAPIDate::class),
        StringScalarSerializer(UAPIDateTime::class),
        StringScalarSerializer(UAPITime::class),
        StringScalarSerializer(UAPIUri::class)
    ).map { it.type to it }.toMap()

internal open class ScalarSerializer<Wrapped : Any, Type : UAPIScalar<Wrapped>>(
    val type: KClass<Type>,
    private val ser: JsonGenerator.(Wrapped) -> Any
) : StdSerializer<Type>(type.java) {
    override fun serialize(value: Type, gen: JsonGenerator, provider: SerializerProvider) {
        if (value.value != null) {
            gen.ser(value.value)
        } else {
            gen.writeNull()
        }
    }
}

internal class StringScalarSerializer<Wrapped: Any, Type : UAPIScalar<Wrapped>>(
    type: KClass<Type>
) : ScalarSerializer<Wrapped, Type>(
    type,
    {value -> this.writeString(value.toString())}
)

internal object IntScalarSerializer : ScalarSerializer<Int, UAPIInt>(
    UAPIInt::class,
    JsonGenerator::writeNumber
)

internal object LongScalarSerializer : ScalarSerializer<Long, UAPILong>(
    UAPILong::class,
    JsonGenerator::writeNumber
)

internal object BooleanScalarSerializer : ScalarSerializer<Boolean, UAPIBoolean>(
    UAPIBoolean::class,
    JsonGenerator::writeBoolean
)
