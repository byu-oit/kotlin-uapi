package edu.byu.uapi.spi.dictionary

import edu.byu.uapi.spi.functional.SuccessOrFailure
import kotlin.reflect.KType

data class TypeFailure(
    val type: KType,
    val message: String,
    val cause: Throwable? = null
)

typealias MaybeTypeFailure<Happy> = SuccessOrFailure<Happy, TypeFailure>
