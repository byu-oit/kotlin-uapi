package edu.byu.uapi.spi

import kotlin.reflect.KType

class UAPITypeError private constructor(
    val type: KType,
    val typeFailure: String,
    cause: Throwable? = null
) : Exception("UAPI Type Error for $type: $typeFailure", cause) {

    companion object {
        fun create(type: KType, typeFailure: String, cause: Throwable? = null): UAPITypeError = UAPITypeError(type, typeFailure, cause)
    }
}

