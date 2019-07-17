package edu.byu.uapi.server.http.integrationtest.dsl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.util.StdConverter
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.util.decodeBase64ToString
import com.github.kittinunf.fuel.util.encodeBase64Url
import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRequest

fun Response.expectReceivedRequestLike(asserts: ActualRequest.() -> Unit) {
    val value = expectHeader(ActualRequest.headerName)
    val decoded = value.decodeBase64ToString()!!
    val request = jackson.readValue<ActualRequest>(decoded)
    request.asserts()
}

data class ActualRequest(
    @get:JsonSerialize(converter = MethodConverterOut::class)
    @JsonDeserialize(converter = MethodConverterIn::class)
    val method: HttpMethod,
    val path: String,
    val headers: Map<String, String>,
    val pathParams: Map<String, String>,
    val queryParams: Map<String, List<String>>,
    val contentType: String?,
    val body: String?,
    val bodyWasIncluded: Boolean
) {
    companion object {
        const val headerName = "xx-test-actual-request"
        const val DEFAULT_INCLUDE_BODY = true
    }
}

@Suppress("FunctionName")
suspend fun ActualRequest(
    req: HttpRequest,
    basePath: String? = null,
    includeBody: Boolean = ActualRequest.DEFAULT_INCLUDE_BODY
): ActualRequest {
    val (contentType, bodyStr) = getBody(req, includeBody)
    val path = if (basePath != null) {
        req.path.removePrefix(basePath)
    } else {
        req.path
    }
    return ActualRequest(
        req.method,
        path,
        req.headers,
        req.pathParams,
        req.queryParams,
        contentType,
        bodyStr,
        includeBody
    )
}

private suspend fun getBody(req: HttpRequest, includeBody: Boolean): Pair<String?, String?> {
    return if (includeBody) {
        req.consumeBody { contentType, stream ->
            contentType to when (contentType) {
                "text/plain"       -> stream.reader().readText()
                "application/json" -> stream.reader().readText()
                else               -> "base64:" + stream.readBytes().encodeBase64Url().toString(Charsets.UTF_8)
            }
        } ?: null to null
    } else {
        null to null
    }
}

private class MethodConverterOut : StdConverter<HttpMethod, String>() {
    override fun convert(value: HttpMethod) = value.name
}

private class MethodConverterIn : StdConverter<String, HttpMethod>() {
    override fun convert(value: String) = HttpMethod(value)
}
