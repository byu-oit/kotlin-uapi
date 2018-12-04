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
    override val path: HttpPathParams = processPathParams(req.params())
    override val headers: Headers = SparkHeaders(req)
    override val query: HttpQueryParams = req.queryMap().toMap().mapValues { setOf(*it.value) }
}

internal const val COMPOUND_PARAM_PREFIX = "compound__"
internal const val COMPOUND_PARAM_SEPARATOR = "__"

private fun processPathParams(incoming: Map<String, String>): HttpPathParams {
    return incoming.asSequence()
        .map { it.key.substring(1) to it.value }
        .flatMap {
            if (it.first.startsWith(COMPOUND_PARAM_PREFIX)) {
                val splitNames = it.first.substring(COMPOUND_PARAM_PREFIX.length).split(COMPOUND_PARAM_SEPARATOR)
                val splitValues = it.second.split(',', limit = splitNames.size)
                splitNames.zip(splitValues).asSequence()
            } else {
                sequenceOf(it)
            }
        }.toMap()
}

class SparkHeaders(private val req: Request) : Headers {
    override fun get(header: String): Set<String> {
        return req.headers(header)?.let { setOf(it) } ?: emptySet()
    }
}
