package edu.byu.uapi.server.http.engines

import java.io.InputStream

/**
 * Tells the HTTP routing mechanism how to get values out of a framework-specific request object.
 * @param[R] the type of the framework request object
 */
interface RequestReader<R> {
    /**
     * Gets the path, relative to the framework's root path.
     */
    fun path(req: R): String

    /**
     * Gets a set of all header names present in the request.
     */
    fun headerNames(req: R): Set<String>

    /**
     * Given a header name, gets the header value.
     * The name is guaranteed to be one of the exact values returned from [headerNames].
     */
    fun headerValue(req: R, name: String): String

    /**
     * Gets all of the query parameters, including repeated instances of the same parameter name.
     */
    fun queryParameters(req: R): Map<String, List<String>>

    /**
     * Gets all of the path parameters in this request. May be 'dirty', or 'raw', as the result will be passed through
     * the [edu.byu.uapi.server.http.path.PathFormatter] defined in [edu.byu.uapi.server.http.engines.HttpEngine.pathFormatter].
     */
    fun pathParameters(req: R): Map<String, String>

    /**
     * Gets the streaming version of the request body. If there is no request body, this should return an empty stream.
     *
     * Guaranteed to only be invoked if the request method is one which has a request body, like 'POST' or 'PUT'.
     */
    suspend fun bodyStream(req: R): InputStream
}
