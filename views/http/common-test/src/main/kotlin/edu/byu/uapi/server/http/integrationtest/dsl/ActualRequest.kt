package edu.byu.uapi.server.http.integrationtest.dsl

import com.fasterxml.jackson.module.kotlin.readValue
import edu.byu.uapi.server.http._internal.DeleteRequest
import edu.byu.uapi.server.http._internal.GetRequest
import edu.byu.uapi.server.http._internal.HttpRequest
import edu.byu.uapi.server.http._internal.HttpRequestWithBody
import edu.byu.uapi.server.http._internal.PatchRequest
import edu.byu.uapi.server.http._internal.PostRequest
import edu.byu.uapi.server.http._internal.PutRequest
import okhttp3.Response
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils

fun Response.expectReceivedRequestLike(asserts: ActualRequest.() -> Unit) {
    val value = expectHeader(ActualRequest.headerName)
    val decoded = Base64.decodeBase64(value)
    val request = jackson.readValue<ActualRequest>(decoded)
    request.asserts()
}

data class ActualRequest(
    val method: String,
    val path: String,
    val headers: Map<String, String>,
    val pathParams: Map<String, String>,
    val queryParams: Map<String, List<String>>,
    val bodySha256: String?
) {
    companion object {
        const val headerName = "xx-test-actual-request"
    }
}

@Suppress("FunctionName", "ComplexMethod")
fun ActualRequest(
    req: HttpRequest,
    body: ByteArray?,
    basePath: String? = null
): ActualRequest {
    val method = when (req) {
        is PostRequest   -> "POST"
        is PutRequest    -> "PUT"
        is PatchRequest  -> "PATCH"
        is GetRequest    -> "GET"
        is DeleteRequest -> "DELETE"
    }
    val bodyStr: String?

    if (req is HttpRequestWithBody && body != null) {
        bodyStr = body.hash()
    } else {
        bodyStr = null
    }

    val path = if (basePath != null) {
        req.path.removePrefix(basePath)
    } else {
        req.path
    }
    return ActualRequest(
        method,
        path,
        req.headers,
        req.pathParams,
        req.queryParams,
        bodyStr
    )
}

fun ByteArray.hash(): String {
    return DigestUtils.sha256Hex(this)
}

