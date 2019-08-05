package edu.byu.uapi.server.http.engines

import java.io.OutputStream

/**
 * Represents an abstract HTTP response. HTTP engines should map from this type to their own response types.
 */
interface HttpResponse {
    /**
     * The HTTP status to return
     */
    val status: Int
    /**
     * The headers, if any, to return
     */
    val headers: Map<String, String>
    /**
     * If not null, this body should be returned to the caller.
     */
    val body: HttpResponseBody?
}

/**
 * Represents an HTTP response body.
 */
interface HttpResponseBody {
    /**
     * The body's content-type header.
     */
    val contentType: String

    /**
     * Call this to write the body to a stream.
     *
     * The body may or may not have already been buffered in memory at this point.
     */
    fun writeTo(stream: OutputStream)
}
