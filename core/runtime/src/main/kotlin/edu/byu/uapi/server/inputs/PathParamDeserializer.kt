package edu.byu.uapi.server.inputs

import edu.byu.uapi.spi.UAPITypeError
import kotlin.reflect.KClassifier
import kotlin.reflect.full.createType

fun UAPITypeError.Companion.create(type: KClassifier, message: String, cause: Throwable? = null): UAPITypeError {
    return UAPITypeError.create(type.createType(), message, cause)
}
