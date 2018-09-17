package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.types.SuccessOrFailure

interface QueryParamDeserializer<T : Any> {
    fun deserializeQueryParams(values: Map<String, Set<String>>): SuccessOrFailure<T, DeserializationFailure<*>>
}
