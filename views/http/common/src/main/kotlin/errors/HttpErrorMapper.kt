package edu.byu.uapi.server.http.errors

import edu.byu.uapi.server.http.engines.HttpResponse

/**
 * Maps exceptions to HTTP Responses.
 */
interface HttpErrorMapper {
    fun map(ex: Throwable): HttpResponse
}

/**
 * Wraps a block to automatically invoke the error mapper in case of an exception.
 */
@Suppress("TooGenericExceptionCaught")
inline fun HttpErrorMapper.runHandlingErrors(block: () -> HttpResponse): HttpResponse {
    return try {
        block()
    } catch (t: Throwable) {
        map(t)
    }
}

