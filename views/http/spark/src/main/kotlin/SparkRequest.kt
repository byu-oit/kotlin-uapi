package edu.byu.uapi.server.http.spark

import edu.byu.uapi.server.http.HttpRequest
import spark.Request

class SparkRequest(
    private val request: Request
) : HttpRequest {
    override val headers: Map<String, String> =
        request.headers().asSequence()
            .map { it.toLowerCase() }
            .associateWith { request.headers(it) }

    override val queryParams: Map<String, List<String>> =
        request.queryParams().associateWith { request.queryParamsValues(it).toList() }

    override val pathParams: Map<String, String> =
        request.params().mapKeys { (k, _) ->
            k.trimStart(':')
        }
}
