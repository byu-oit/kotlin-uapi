package edu.byu.uapidsl.converters

import edu.byu.uapidsl.types.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KType


interface ScalarSerializerRegistry {
    fun <Type : Any> deserializerFor(type: KType): ScalarDeserializer<Type, *>
    fun <Type : Any> serializerFor(type: KType): ScalarSerializer<Type, *>
}


interface ScalarDeserializer<out Type : Any, ScalarType : UAPIScalar<*>> {
    fun deserialize(incoming: ScalarType, actualType: KClass<in Type>): Type?
}

interface ScalarSerializer<in Type, ScalarType : UAPIScalar<*>> {
    fun serialize(outgoing: Type?): ScalarType
}

interface BidirectionalScalarConverter<Type : Any, ScalarType : UAPIScalar<*>> : ScalarDeserializer<Type, ScalarType>, ScalarSerializer<Type, ScalarType>

private class SimpleSerialization<Type : Any, ScalarType : UAPIScalar<Type>>(
    private val constructor: (Type?) -> ScalarType
) : BidirectionalScalarConverter<Type, ScalarType> {
    override fun deserialize(incoming: ScalarType, actualType: KClass<in Type>) = incoming.value

    override fun serialize(outgoing: Type?) = constructor(outgoing)
}

private class SlightlyLessSimpleSerialization<Type: Any, ScalarType: Any, Scalar: UAPIScalar<ScalarType>>(
    private val constructor: (Type?) -> Scalar,
    private val valueTransformer: (ScalarType) -> Type
): BidirectionalScalarConverter<Type, Scalar> {
    override fun deserialize(incoming: Scalar, actualType: KClass<in Type>): Type? {
        val value = incoming.value
        return if (value == null) {
            null
        } else {
            valueTransformer(value)
        }
    }

    override fun serialize(outgoing: Type?): Scalar = constructor(outgoing)

}

private typealias ScalarConstructor<Type, Scalar> = (Type?) -> Scalar

private inline fun <reified Type : Any, Scalar : UAPIScalar<Type>> simple(noinline constructor: ScalarConstructor<Type, Scalar>)
    = Type::class to SimpleSerialization(constructor)


private inline fun <reified Type : Any, ScalarType: Any, Scalar : UAPIScalar<ScalarType>> lessSimple(
    noinline constructor: ScalarConstructor<Type, Scalar>,
    noinline valueTransformer: (ScalarType) -> Type
)= Type::class to SlightlyLessSimpleSerialization(constructor, valueTransformer)


val defaultScalars: Map<KClass<*>, BidirectionalScalarConverter<*, *>> = mapOf(
    simple(::UAPIInt),
    simple(::UAPILong),
    simple<BigDecimal, UAPIDecimal>(::UAPIDecimal),
    lessSimple(::UAPIDecimal, BigDecimal::toFloat),
    lessSimple(::UAPIDecimal, BigDecimal::toDouble),
    simple(::UAPIString),
    simple(::UAPIByteArray),
    simple(::UAPIBoolean),
    simple(::UAPIDate),
    simple(::UAPITime),
    simple(::UAPIUri),
    simple<OffsetDateTime, UAPIDateTime>(::UAPIDateTime),
    lessSimple(::UAPIDateTime, OffsetDateTime::toZonedDateTime),
    lessSimple(::UAPIDateTime, OffsetDateTime::toInstant)
)

