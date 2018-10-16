package edu.byu.uapi.server.inputs

import edu.byu.uapi.spi.dictionary.DeserializationFailure
import edu.byu.uapi.spi.scalars.ScalarType
import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.input.PathParamDeserializer
import edu.byu.uapi.spi.functional.SuccessOrFailure
import edu.byu.uapi.spi.functional.asFailure
import kotlin.reflect.KClass

class ScalarPathParamDeserializer<T: Any>(
    private val type: ScalarType<T>
): PathParamDeserializer<T> {
    override fun deserializePathParams(values: Map<String, String>): SuccessOrFailure<T, DeserializationFailure<*>> {
        if (values.size != 1) {
            return fail(type.type, "Expected exactly 1 path parameter")
        }
        return type.fromString(values.values.first())
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
internal fun <T : Any> fail(type: KClass<T>, message: String): Failure<DeserializationFailure<T>> = DeserializationFailure<T>(type, message).asFailure()

