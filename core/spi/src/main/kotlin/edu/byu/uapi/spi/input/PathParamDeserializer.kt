package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.dictionary.MaybeTypeFailure

interface PathParamDeserializer<T : Any> {
    fun deserializePathParams(values: Map<String, String>): MaybeTypeFailure<T>
}
