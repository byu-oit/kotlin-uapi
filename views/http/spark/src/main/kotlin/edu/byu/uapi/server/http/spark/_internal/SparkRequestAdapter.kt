package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.BaseHttpRequest
import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.unformatValues
import spark.Request
import java.io.ByteArrayInputStream
import java.io.InputStream

internal class SparkRequestAdapter(
    private val request: Request,
    pathParts: RoutePath
) : BaseHttpRequest(
    method = HttpMethod(request.requestMethod()),
    headers = request.uapiHeaders(),
    queryParams = request.uapiQuery(),
    pathParams = request.uapiPath(pathParts)
) {

    override val path: String
        get() = request.pathInfo()
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

private fun Request.uapiPath(pathParts: RoutePath): Map<String, String> {
    return sparkPaths.unformatValues(pathParts, this.params())
}
