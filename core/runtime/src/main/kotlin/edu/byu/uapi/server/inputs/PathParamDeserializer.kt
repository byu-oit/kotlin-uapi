package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.spi.UAPITypeError
import edu.byu.uapi.spi.dictionary.TypeFailure
import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.functional.asFailure
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.full.createType

internal inline fun <reified T : Any> typeFailure(message: String, cause: Throwable? = null) = typeFailure(T::class, message, cause)
internal fun typeFailure(type: KClassifier, message: String, cause: Throwable? = null): Failure<TypeFailure> = TypeFailure(type.createType(), message, cause).asFailure()
internal fun typeFailure(type: KType, message: String, cause: Throwable? = null): Failure<TypeFailure> = TypeFailure(type, message, cause).asFailure()

fun typeError(type: KClassifier, message: String, cause: Throwable? = null): Nothing {
    throw UAPITypeError(type, message, cause)
}

fun typeError(type: KType, message: String, cause: Throwable? = null): Nothing {
    throw UAPITypeError(type, message, cause)
}
