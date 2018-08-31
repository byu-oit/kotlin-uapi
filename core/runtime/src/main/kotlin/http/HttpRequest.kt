package http

interface HttpRequest {

    val path: PathParams
    val headers: Headers

}

interface HttpBodyRequest : HttpRequest {

    val body: RequestBody

}

interface GetRequest : HttpRequest {

    val query: QueryParams

}

interface PostRequest : HttpBodyRequest
interface PutRequest : HttpBodyRequest
interface PatchRequest : HttpBodyRequest
interface DeleteRequest : HttpRequest
interface OptionsRequest : HttpRequest

interface RequestBody // TODO: Handle cases of streamed or string'ed bodies. Maybe with sealed class? How does that work in Multiplatform projects?

data class StringRequestBody(val body: String): RequestBody {

}

typealias PathParams = Map<String, String>
typealias Headers = Map<String, Set<String>>
typealias QueryParams = Map<String, Set<String>>
