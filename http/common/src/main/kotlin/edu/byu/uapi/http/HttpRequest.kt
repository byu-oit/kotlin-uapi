package edu.byu.uapi.http

interface HttpRequest {
    val method: HttpMethod
    val path: HttpPathParams
    val headers: HttpHeaders
    val query: HttpQueryParams
    val rawPath: String
    val body: RequestBody?
}

interface RequestBody // TODO: Handle cases of streamed or number'ed bodies. Maybe with sealed class? How does that work in Multiplatform projects?

data class StringRequestBody(val body: String): RequestBody {

}

typealias HttpPathParams = Map<String, String>
typealias HttpHeaders = Map<String, Set<String>>
typealias HttpQueryParams = Map<String, Set<String>>
