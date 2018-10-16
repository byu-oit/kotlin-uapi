package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.dictionary.DeserializationFailure
import edu.byu.uapi.spi.functional.SuccessOrFailure

interface PathParamDeserializer<T : Any> {
    fun deserializePathParams(values: Map<String, String>): SuccessOrFailure<T, DeserializationFailure<*>>
}
