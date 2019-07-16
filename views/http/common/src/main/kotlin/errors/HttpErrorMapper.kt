package edu.byu.uapi.server.http.errors

import edu.byu.uapi.server.http.HttpResponse

interface HttpErrorMapper {
    fun map(ex: Throwable): HttpResponse
}

inline fun HttpErrorMapper.runHandlingErrors(block: () -> HttpResponse): HttpResponse {
    return try {
        block()
    } catch (t: Throwable) {
        map(t)
    }
}

