package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.BaseHttpRequest
import edu.byu.uapi.server.http.HttpMethod
import spark.Request
import java.io.ByteArrayInputStream
import java.io.InputStream

internal class SparkRequestAdapter(
    private val request: Request
) : BaseHttpRequest(
    method = HttpMethod(request.requestMethod()),
    headers = request.uapiHeaders(),
    queryParams = request.uapiQuery(),
    pathParams = request.uapiPath()
) {
    override suspend fun bodyAsStream(): InputStream {
        return ByteArrayInputStream(request.bodyAsBytes())
    }
}

private fun Request.uapiHeaders(): Map<String, String> {
    return this.headers().asSequence()
        .map { it.toLowerCase() }
        .associateWith { this.headers(it) }
}

private fun Request.uapiQuery(): Map<String, List<String>> {
    return this.queryParams()
        .associateWith { this.queryParamsValues(it).toList() }
}

private fun Request.uapiPath(): Map<String, String> {
    return this.params().mapKeys { (k, _) ->
        k.trimStart(':')
    }
}
