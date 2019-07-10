package edu.byu.uapi.server.http.test.fixtures

import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.HttpResponse
import edu.byu.uapi.server.http.HttpResponseBody
import java.io.OutputStream

class MockHttpHandler(
    val response: HttpResponse = FakeHttpResponse(200, null)
) : HttpHandler {
    val calls = mutableListOf<HttpRequest>()
    override suspend fun handle(request: HttpRequest): HttpResponse {
        calls += request
        return response
    }
}

fun fakeResponse(init: FakeResponseInit.() -> Unit): FakeHttpResponse {
    return FakeResponseInit()
        .apply { init() }
        .resp
}

class FakeResponseInit {
    internal var resp = FakeHttpResponse(200, null)

    var status: Int
        get() = resp.status
        set(value) {
            resp = resp.copy(status = value)
        }

    fun header(key: String, value: String) {
        resp = resp.copy(headers = resp.headers + (key to value))
    }

    fun body(value: String, type: String) {
        resp = resp.copy(body = FakeHttpResponseBody(value, type))
    }
    fun noBody() {
        resp = resp.copy(body = null)
    }
}

data class FakeHttpResponse(
    override val status: Int,
    override val body: HttpResponseBody?,
    override val headers: Map<String, String> = emptyMap()
) : HttpResponse

class FakeHttpResponseBody(
    val content: String,
    override val contentType: String
) : HttpResponseBody {
    override fun writeTo(stream: OutputStream) {
        stream.write(content.toByteArray())
    }
}
