package edu.byu.uapi.server.spi.errors


/**
 * An error type denoting an unexpected, invalid state has been encountered. For example,
 * if an underlying HTTP server behaves unexpectedly by matching a route but not including
 * the relevant path parameters.
 *
 * This is the sort of error that requires fixes to the UAPI runtime or its underlying libraries,
 * not to user code. End-client errors (like passing bad data) should extend UAPIClientError
 * and consuming application errors (like misconfiguring the runtime) should extend UAPIApplicationError
 */
open class UAPIInternalError(
    override val message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)


