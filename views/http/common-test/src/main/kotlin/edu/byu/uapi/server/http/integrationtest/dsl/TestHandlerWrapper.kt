package edu.byu.uapi.server.http.integrationtest.dsl

import com.github.kittinunf.fuel.util.encodeBase64UrlToString
import edu.byu.uapi.server.http.BodyConsumer
import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.HttpResponse

internal class TestHandlerWrapper(
    val basePath: String,
    val handler: TestHttpHandler
) : HttpHandler {
    override suspend fun handle(request: HttpRequest): HttpResponse {
        val tr = TestRequestWrapper(request)
        val req = ActualRequest(tr, basePath)
        val resp = handler(tr)
        return TestResponseWrapper(resp, req)
    }
}

internal class TestRequestWrapper(val request: HttpRequest) : HttpRequest by request {
    //    override val path: String = request.path.removePrefix(basePath)
    private lateinit var body: Pair<String?, ByteArray?>

    override suspend fun <T> consumeBody(consumer: BodyConsumer<T>): T? {
        val (contentType, bytes) = if (this::body.isInitialized) {
            this.body
        } else {
            request.consumeBody { contentType, stream -> contentType to stream.readBytes() }
                ?: null to null
        }
        return if (contentType != null && bytes != null) {
            consumer(contentType, bytes.inputStream())
        } else {
            null
        }
    }
}

internal class TestResponseWrapper(val response: HttpResponse, receivedRequest: ActualRequest) :
    HttpResponse by response {
    override val headers: Map<String, String> =
        response.headers + Pair(
            ActualRequest.headerName,
            jackson.writeValueAsString(receivedRequest).encodeBase64UrlToString()
        )
}
