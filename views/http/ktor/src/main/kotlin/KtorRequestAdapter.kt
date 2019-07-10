package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.BaseHttpRequest
import edu.byu.uapi.server.http.HttpMethod
import io.ktor.application.ApplicationCall
import io.ktor.request.httpMethod
import io.ktor.request.receiveStream
import io.ktor.util.filter
import io.ktor.util.flattenEntries
import io.ktor.util.toMap
import java.io.InputStream

internal class KtorRequestAdapter(
    private val call: ApplicationCall
) : BaseHttpRequest(
    method = call.getUapiMethod(),
    headers = call.getUapiHeaders(),
    queryParams = call.getUapiQuery(),
    pathParams = call.getUapiPath()
) {
    override suspend fun bodyAsStream(): InputStream {
        return call.receiveStream()
    }
}

private fun ApplicationCall.getUapiMethod() =
    HttpMethod(request.httpMethod.value)

private fun ApplicationCall.getUapiHeaders() =
    request.headers.flattenEntries()
        .associate { it.first.toLowerCase() to it.second }

private fun ApplicationCall.getUapiQuery() =
    request.queryParameters.toMap()

private fun ApplicationCall.getUapiPath() =
    parameters
        .filter { k, _ -> k !in request.queryParameters }
        .flattenEntries()
        .toMap()

