package edu.byu.uapi.http

interface HttpRequest {
    val method: HttpMethod
    val path: PathParams
    val headers: Headers
    val query: QueryParams
    val rawPath: String
    val body: RequestBody?
}

interface RequestBody // TODO: Handle cases of streamed or string'ed bodies. Maybe with sealed class? How does that work in Multiplatform projects?

data class StringRequestBody(val body: String): RequestBody {

}

typealias PathParams = Map<String, String>
typealias Headers = Map<String, Set<String>>
typealias QueryParams = Map<String, Set<String>>
