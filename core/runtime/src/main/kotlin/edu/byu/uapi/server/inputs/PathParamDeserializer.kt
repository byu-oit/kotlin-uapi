package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.scalars.ScalarConverter
import edu.byu.uapi.server.types.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*
import kotlin.reflect.KClass

interface PathParamDeserializer<T : Any> {
    fun deserializePathParams(values: Map<String, String>): SuccessOrFailure<T, DeserializationFailure<*>>
}

class ScalarPathParamDeserializer<T: Any>(
    private val converter: ScalarConverter<T>
): PathParamDeserializer<T> {
    override fun deserializePathParams(values: Map<String, String>): SuccessOrFailure<T, DeserializationFailure<*>> {
        if (values.size != 1) {
            return fail(converter.type, "Expected exactly 1 path parameter")
        }
        return converter.fromString(values.values.first())
    }
}

class ReflectivePathParamDeserializer<T : Any>(
    val type: KClass<T>
) : PathParamDeserializer<T> {

    init {
        if (type.isData) {

        }
    }

    override fun deserializePathParams(values: Map<String, String>): SuccessOrFailure<T, DeserializationFailure<*>> {
        TODO("not implemented")
    }
}

internal inline fun <reified T : Any> fail(message: String) = fail(T::class, message)
internal fun <T : Any> fail(type: KClass<T>, message: String): Failure<DeserializationFailure<T>> = DeserializationFailure(type, message).asFailure()

