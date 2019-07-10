package edu.byu.uapi.server.http

import java.io.IOException
import java.io.InputStream

interface HttpRequest {
    /**
     * Gets the HTTP Method
     */
    val method: HttpMethod
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

    suspend fun <T> consumeBody(consumer: BodyConsumer<T>): T?
}

typealias BodyConsumer<T> = suspend (contentType: String, stream: InputStream) -> T

abstract class BaseHttpRequest(
    override val method: HttpMethod,
    override val headers: Map<String, String>,
    override val pathParams: Map<String, String>,
    override val queryParams: Map<String, List<String>>
) : HttpRequest {
    protected abstract suspend fun bodyAsStream(): InputStream

    private var _consumed = false

    override suspend fun <T> consumeBody(consumer: BodyConsumer<T>): T? {
        if (_consumed) {
            throw IOException("Body has already been consumed")
        }
        _consumed = true
        bodyAsStream().buffered().use { stream ->
            if (!method.allowsBodyInUAPI) {
                return null
            }
            val type = headers["content-type"]
                ?: return null

            stream.mark(1)
            val byte = stream.read()
            stream.reset()

            if (byte == -1) {
                return null
            }
            return consumer(type, stream)
        }
    }
}

