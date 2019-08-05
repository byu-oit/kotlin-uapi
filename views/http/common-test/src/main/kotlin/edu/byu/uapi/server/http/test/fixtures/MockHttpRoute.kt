package edu.byu.uapi.server.http.test.fixtures

import edu.byu.uapi.server.http.engines.HttpResponse
import edu.byu.uapi.server.http.engines.HttpResponseBody
import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.engines.RouteMethod
import java.io.OutputStream

class MockHttpRoute<R : Any>(
    val response: HttpResponse = FakeHttpResponse(200, null),
    override val method: RouteMethod = RouteMethod.GET,
    override val pathSpec: String = "/foo",
    override val consumes: String? = null,
    override val produces: String? = null
) : HttpRoute<R> {
    val calls = mutableListOf<R>()

    override suspend fun dispatch(request: R): HttpResponse {
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
