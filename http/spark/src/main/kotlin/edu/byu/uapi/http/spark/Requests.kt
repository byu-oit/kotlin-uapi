package edu.byu.uapi.http.spark

import edu.byu.uapi.http.*
import edu.byu.uapi.spi.requests.Headers
import spark.Request

class SparkRequest(
    private val req: Request
) : HttpRequest {
    override val method: HttpMethod = HttpMethod.valueOf(req.requestMethod().toUpperCase())
    override val rawPath: String
        get() = TODO("not implemented")
    override val body: HttpRequestBody? = req.body()?.let { StringHttpRequestBody(it) }
    override val path: HttpPathParams = req.params().mapKeys { it.key.substring(1) }
    override val headers: Headers = SparkHeaders(req)
    override val query: HttpQueryParams = req.queryMap().toMap().mapValues { setOf(*it.value) }
}

class SparkHeaders(private val req: Request): Headers {
    override fun get(header: String): Set<String> {
        return req.headers(header)?.let { setOf(it) } ?: emptySet()
    }
}
