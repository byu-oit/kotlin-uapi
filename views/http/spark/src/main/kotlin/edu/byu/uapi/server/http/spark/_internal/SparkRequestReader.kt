package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.engines.RequestReader
import spark.Request
import java.io.InputStream

internal object SparkRequestReader : RequestReader<Request> {
    override fun path(req: Request): String {
        return req.pathInfo()
    }

    override fun headerNames(req: Request): Set<String> {
        return req.headers()
    }

    override fun headerValue(req: Request, name: String): String {
        return req.headers(name)
    }

    override fun queryParameters(req: Request): Map<String, List<String>> {
        return req.queryParams()
            .associateWith { req.queryParamsValues(it).toList() }
    }

    override fun pathParameters(req: Request): Map<String, String> {
        return req.params()
    }

    override suspend fun bodyStream(req: Request): InputStream {
        return req.bodyAsBytes().inputStream()
    }
}
