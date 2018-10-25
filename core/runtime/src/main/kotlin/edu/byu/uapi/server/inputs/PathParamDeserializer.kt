package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.spi.UAPITypeError
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.dictionary.TypeFailure
import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.functional.asFailure
import edu.byu.uapi.spi.input.PathParamDeserializer
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class ScalarPathParamDeserializer<T: Any>(
    private val type: ScalarType<T>
): PathParamDeserializer<T> {
    override fun deserializePathParams(values: Map<String, String>): MaybeTypeFailure<T> {
        if (values.size != 1) {
            return typeFailure(type.type, "Expected exactly 1 path parameter")
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

    override fun deserializePathParams(values: Map<String, String>): MaybeTypeFailure<T> {
        TODO("not implemented")
    }
}

internal inline fun <reified T : Any> typeFailure(message: String) = typeFailure(T::class, message)
internal fun typeFailure(type: KClassifier, message: String): Failure<TypeFailure> = TypeFailure(type.createType(), message).asFailure()
internal fun typeFailure(type: KType, message: String): Failure<TypeFailure> = TypeFailure(type, message).asFailure()

fun typeError(type: KClassifier, message: String): Nothing {
    throw UAPITypeError(type, message)
}

fun typeError(type: KType, message: String): Nothing {
    throw UAPITypeError(type, message)
}
