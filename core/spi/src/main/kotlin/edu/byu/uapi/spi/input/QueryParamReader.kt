package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.dictionary.MaybeTypeFailure

interface QueryParamReader<T : Any> {
    fun deserializeQueryParams(values: Map<String, Set<String>>): MaybeTypeFailure<T>
}
