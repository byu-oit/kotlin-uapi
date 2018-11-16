package edu.byu.uapi.http

import edu.byu.uapi.spi.requests.Headers

interface HttpRequest {
    val method: HttpMethod
    val path: HttpPathParams
    val headers: Headers
    val query: HttpQueryParams
    val rawPath: String
    val body: RequestBody?
}

interface RequestBody // TODO: Handle cases of streamed or number'ed bodies. Maybe with sealed class? How does that work in Multiplatform projects?

data class StringRequestBody(val body: String): RequestBody {

}

typealias HttpPathParams = Map<String, String>
typealias HttpQueryParams = Map<String, Set<String>>
