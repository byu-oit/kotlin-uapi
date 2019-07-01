package edu.byu.uapi.spi.input

typealias PathParams = Map<String, String>

@Deprecated("Replaced with IdParamReader")
interface PathParamReader<T : Any> {
    fun read(values: PathParams): T
}

