package edu.byu.uapi.server.http.integrationtest.dsl

import com.github.kittinunf.fuel.util.encodeBase64UrlToString
import edu.byu.uapi.server.http.BodyConsumer
import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.HttpResponse

internal const val DEFAULT_TRACK_ACTUAL_REQUEST = true

internal class TestHandlerWrapper(
    private val basePath: String,
    val handler: TestHttpHandler,
    private val trackActualRequestBody: Boolean = DEFAULT_TRACK_ACTUAL_REQUEST
) : HttpHandler {
    override suspend fun handle(request: HttpRequest): HttpResponse {
        val tr = if (trackActualRequestBody) {
            TestRequestWrapper(request)
        } else {
            request
        }
        val req = ActualRequest(tr, basePath, trackActualRequestBody)
        val resp = handler(tr)
        return TestResponseWrapper(resp, req)
    }
}

internal class TestRequestWrapper(val request: HttpRequest) : HttpRequest by request {
    private lateinit var body: Pair<String?, ByteArray?>

    override suspend fun <T> consumeBody(consumer: BodyConsumer<T>): T? {
        if (!this::body.isInitialized) {
            this.body = request.consumeBody { contentType, stream -> contentType to stream.readBytes() }
                ?: null to null
        }
        val (contentType, bytes) = this.body
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
