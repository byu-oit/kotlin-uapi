package edu.byu.uapi.server.http.integrationtest

import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.encodeBase64Url
import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.integrationtest.dsl.RoutingInit
import edu.byu.uapi.server.http.integrationtest.dsl.TestHttpHandler

val echoMime = "application/x.echoHandler+json"
val echoHandler: TestHttpHandler = {
    TestResponse.Body(
        200,
        jackson.writeValueAsString(
            EchoResponse(this)
        ),
        echoMime
    )
}

fun Response.assertEchoed(assertEchoed: EchoResponse.() -> Unit) {
    assertJsonParseableAs<EchoResponse>(echoMime).apply(assertEchoed)
}

@Suppress("FunctionName")
private suspend fun EchoResponse(req: HttpRequest): EchoResponse {
    val (contentType, bodyStr) = req.consumeBody { contentType, stream ->
        contentType to when (contentType) {
            "text/plain"       -> stream.reader().readText()
            "application/json" -> stream.reader().readText()
            else               -> "base64:" + stream.readBytes().encodeBase64Url().toString(Charsets.UTF_8)
        }
    } ?: null to null
    return EchoResponse(
        req.method.name,
        req.path,
        req.headers,
        req.pathParams,
        req.queryParams,
        contentType,
        bodyStr
    )
}

data class EchoResponse(
    val method: String,
    val path: String,
    val headers: Map<String, String>,
    val pathParams: Map<String, String>,
    val queryParams: Map<String, List<String>>,
    val contentType: String?,
    val body: String?
)

fun RoutingInit.echoGet(consumes: String? = null, produces: String? = null) {
    get(consumes, produces, echoHandler)
}

fun RoutingInit.echoPost(consumes: String? = null, produces: String? = null) {
    post(consumes, produces, echoHandler)
}

fun RoutingInit.echoPut(consumes: String? = null, produces: String? = null) {
    put(consumes, produces, echoHandler)
}

fun RoutingInit.echoPatch(consumes: String? = null, produces: String? = null) {
    patch(consumes, produces, echoHandler)
}

fun RoutingInit.echoDelete(consumes: String? = null, produces: String? = null) {
    delete(consumes, produces, echoHandler)
}
