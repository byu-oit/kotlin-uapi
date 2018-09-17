package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.types.SuccessOrFailure
import kotlin.reflect.KClass

interface DeserializationContext {
    fun <Type : Any> pathDeserializer(type: KClass<Type>): SuccessOrFailure<PathParamDeserializer<Type>, DeserializationFailure<*>>
}

data class DeserializationFailure<T: Any>(
    val type: KClass<T>,
    val message: String,
    val cause: Throwable? = null
) {
    fun asError(): DeserializationError = DeserializationError(type, message, cause)
}

class DeserializationError(
    val type: KClass<*>,
    message: String,
    cause: Throwable? = null
): Exception("Error deserializing type $type: $message", cause)
