@file:Suppress("ForbiddenComment")
package edu.byu.uapi.server.http

import edu.byu.uapi.server.http.errors.UAPIHttpMissingBodyError
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

interface HttpRequest {
    /**
     * Gets the HTTP Method
     */
    val method: HttpMethod

    val path: String
    /**
     * Gets the HTTP headers from the request. Header keys should be normalized to lower-case strings.
     */
    val headers: Map<String, String>
    /**
     * Get the query parameters from the request, or an empty map if there are none.
     */
    val queryParams: Map<String, List<String>>
    /**
     * Gets the path parameters from the request.
     */
    val pathParams: Map<String, String>

    //TODO: Move this to a top-level extension function. That'll allow us to do things like add a contract and such
    //  This can be done by adding:
    //  class RequestBody(internal val type: String, internal val stream: InputStream) {  }
    //  The internal vals make it so that we're the only package that can actually get at the contents, which means
    //  that we can still have our fancy make-sure-it's-always-closed wrappers.
    suspend fun <T> consumeBody(consumer: BodyConsumer<T>): T?

}

class UseOnceStreamWrapper(
    private val stream: InputStream
) : AutoCloseable by stream {
    private val consumedAt = AtomicReference<Throwable>(null)

    internal fun consume(): InputStream {
        val consumed = consumedAt.getAndSet(Throwable("Body was consumed at:"))
        if (consumed != null) {
            throw IOException(
                "Body has already been consumed; consumption point is listed as the cause of this exception",
                consumed
            )
        }

        return stream
    }
}

//suspend inline fun <T> HttpRequest.requireBody(
// crossinline consumer: suspend (contentType: String, stream: InputStream) -> T
//): T {
suspend inline fun <T> HttpRequest.requireBody(crossinline consumer: BodyConsumer<T>): T {
    var consumed = false

    val result = this.consumeBody { type, stream ->
        consumed = true
        consumer(type, stream)
    }

    if (!consumed) {
        throw UAPIHttpMissingBodyError("invoking requireBody()")
    }

    @Suppress("UNCHECKED_CAST")
    // By casting this, we allow the actual type of T to be nullable while still making the compiler happy.
    return result as T
}

typealias BodyConsumer<T> = suspend (contentType: String, stream: InputStream) -> T

abstract class BaseHttpRequest(
    override val method: HttpMethod,
    override val headers: Map<String, String>,
    override val pathParams: Map<String, String>,
    override val queryParams: Map<String, List<String>>
) : HttpRequest {
    protected abstract suspend fun bodyAsStream(): InputStream

    private var _consumed = AtomicBoolean(false)

    override suspend fun <T> consumeBody(consumer: BodyConsumer<T>): T? {
        if (_consumed.getAndSet(true)) {
            throw IOException("Body has already been consumed")
        }
        bodyAsStream().buffered().use { stream ->
            val type = headers["content-type"]
            val bodyNotAllowed = !method.allowsBodyInHttp

            if (bodyNotAllowed || type == null || stream.isEmpty()) {
                return null
            }
            return consumer(type, stream)
        }
    }
}

private fun BufferedInputStream.isEmpty(): Boolean {
    // Set a marker, read a byte, reset the the buffer back to the marker - if byte is -1 (EOF), the stream is empty
    mark(1)
    val byte = read()
    reset()

    return byte == -1
}

