package edu.byu.uapi.server.http._internal

import java.io.InputStream

sealed class HttpRequest {
    /**
     * Gets the HTTP Method
     */
//    abstract val method: HttpMethod

    abstract val path: String
    /**
     * Gets the HTTP headers from the request. Header keys should be normalized to lower-case strings.
     */
    abstract val headers: Map<String, String>
    /**
     * Get the query parameters from the request, or an empty map if there are none.
     */
    abstract val queryParams: Map<String, List<String>>
    /**
     * Gets the pathSpec parameters from the request.
     */
    abstract val pathParams: Map<String, String>
}

sealed class HttpRequestWithBody: HttpRequest() {
    abstract val inputStream: InputStream
}

const val HTTP_CONTENT_TYPE = "content-type"
const val HTTP_ACCEPT = "accept"

fun HttpRequestWithBody.contentType(): String? {
    return headers[HTTP_CONTENT_TYPE]
}

fun HttpRequest.acceptType(): String? {
    return headers[HTTP_ACCEPT]
}

class GetRequest(
    override val path: String,
    override val headers: Map<String, String>,
    override val queryParams: Map<String, List<String>>,
    override val pathParams: Map<String, String>
): HttpRequest()

class PostRequest(
    override val path: String,
    override val headers: Map<String, String>,
    override val queryParams: Map<String, List<String>>,
    override val pathParams: Map<String, String>,
    override val inputStream: InputStream
): HttpRequestWithBody()

class PutRequest(
    override val path: String,
    override val headers: Map<String, String>,
    override val queryParams: Map<String, List<String>>,
    override val pathParams: Map<String, String>,
    override val inputStream: InputStream
): HttpRequestWithBody()

class PatchRequest(
    override val path: String,
    override val headers: Map<String, String>,
    override val queryParams: Map<String, List<String>>,
    override val pathParams: Map<String, String>,
    override val inputStream: InputStream
): HttpRequestWithBody()

class DeleteRequest(
    override val path: String,
    override val headers: Map<String, String>,
    override val queryParams: Map<String, List<String>>,
    override val pathParams: Map<String, String>
): HttpRequest()
