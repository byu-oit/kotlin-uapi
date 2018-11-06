package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.dictionary.MaybeTypeFailure

typealias PathParams = Map<String, String>

@Deprecated("Replaced with IdParamReader")
interface PathParamReader<T : Any> {
    fun read(values: PathParams): MaybeTypeFailure<T>
}

