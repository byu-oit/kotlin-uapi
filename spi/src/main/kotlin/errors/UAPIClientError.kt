package edu.byu.uapi.server.spi.errors

sealed class UAPIClientError(
    override val message: String,
    cause: Throwable? = null
) : Exception(message, cause)

open class UAPIMissingIdParamValueError protected constructor(
    message: String,
    cause: Throwable? = null
) : UAPIClientError(message, cause) {
    constructor(paramNames: List<String>) : this(
        "Missing values for ID parameters " + paramNames.joinToString { "'$it'" }
    )
}
