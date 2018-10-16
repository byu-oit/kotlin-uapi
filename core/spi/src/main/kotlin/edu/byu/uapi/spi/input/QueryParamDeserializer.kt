package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.dictionary.DeserializationFailure
import edu.byu.uapi.spi.functional.SuccessOrFailure

interface QueryParamDeserializer<T : Any> {
    fun deserializeQueryParams(values: Map<String, Set<String>>): SuccessOrFailure<T, DeserializationFailure<*>>
}
