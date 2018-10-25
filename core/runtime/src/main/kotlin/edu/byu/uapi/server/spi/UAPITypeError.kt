package edu.byu.uapi.server.spi

import edu.byu.uapi.spi.dictionary.TypeFailure
import kotlin.reflect.KClassifier
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class UAPITypeError(
    val type: KType,
    val typeFailure: String,
    cause: Throwable? = null
) : Exception("UAPI Type Error for $type: $typeFailure", cause) {
    constructor(
        type: KClassifier,
        message: String,
        cause: Throwable? = null
    ) : this(type.createType(), message, cause)
}

fun TypeFailure.asError(): UAPITypeError = UAPITypeError(type, message, cause)
