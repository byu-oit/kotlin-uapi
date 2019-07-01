package edu.byu.uapi.spi.dictionary

import kotlin.reflect.KType

data class TypeFailure(
    val type: KType,
    val message: String,
    val cause: Throwable? = null
)
