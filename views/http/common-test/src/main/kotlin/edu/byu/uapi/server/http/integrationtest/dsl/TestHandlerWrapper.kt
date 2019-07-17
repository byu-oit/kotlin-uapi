package edu.byu.uapi.server.http.integrationtest.dsl

import com.github.kittinunf.fuel.util.encodeBase64UrlToString
import edu.byu.uapi.server.http._internal.HttpHandler
import edu.byu.uapi.server.http._internal.HttpRequest
import edu.byu.uapi.server.http._internal.HttpRequestWithBody
import edu.byu.uapi.server.http.engines.HttpResponse
import edu.byu.uapi.server.http._internal.PatchRequest
import edu.byu.uapi.server.http._internal.PostRequest
import edu.byu.uapi.server.http._internal.PutRequest

internal class TestHandlerWrapper<R : HttpRequest>(
    private val basePath: String,
    val handler: TestHttpHandler<R>
) : HttpHandler<R> {
    override suspend fun handle(request: R): HttpResponse {
        val (wrapped, body) = wrapAndExtractBody(request)
        val req = ActualRequest(wrapped, body, basePath)
        val resp = handler(wrapped)
        return TestResponseWrapper(resp, req)
    }
}

@Suppress("UNCHECKED_CAST")
private fun <R : HttpRequest> wrapAndExtractBody(req: R): Pair<R, ByteArray?> {
    if (req !is HttpRequestWithBody) return req to null
    val body = req.inputStream.readBytes()
    val stream = body.inputStream()
    return when(req) {
        is PostRequest  -> PostRequest(req.path, req.headers, req.queryParams, req.pathParams, stream) to body
        is PutRequest   -> PutRequest(req.path, req.headers, req.queryParams, req.pathParams, stream) to body
        is PatchRequest -> PatchRequest(req.path, req.headers, req.queryParams, req.pathParams, stream) to body
    } as Pair<R, ByteArray?>
}

internal class TestResponseWrapper(val response: HttpResponse, receivedRequest: ActualRequest) :
    HttpResponse by response {
    override val headers: Map<String, String> =
        response.headers + Pair(
            ActualRequest.headerName,
            jackson.writeValueAsString(receivedRequest).encodeBase64UrlToString()
        )
}
