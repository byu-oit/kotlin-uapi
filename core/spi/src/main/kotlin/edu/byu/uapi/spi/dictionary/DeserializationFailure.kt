package edu.byu.uapi.spi.dictionary

import kotlin.reflect.KClassifier

data class DeserializationFailure<T>(
    val type: KClassifier,
    val message: String,
    val cause: Throwable? = null
) {
    fun asError(): DeserializationError = DeserializationError(type, message, cause)
}

class DeserializationError(
    val type: KClassifier,
    message: String,
    cause: Throwable? = null
) : Exception("Error deserializing type $type: $message", cause)
