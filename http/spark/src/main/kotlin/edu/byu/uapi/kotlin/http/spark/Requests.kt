package edu.byu.uapi.kotlin.http.spark

import edu.byu.uapi.http.*
import spark.Request

class SparkRequest(
    private val req: Request
) : HttpRequest {
    override val method: HttpMethod = HttpMethod.valueOf(req.requestMethod().toUpperCase())
    override val rawPath: String
        get() = TODO("not implemented")
    override val body: RequestBody?
        get() = TODO("not implemented")
    override val path: PathParams = req.params()
    override val headers: Headers = req.headers().associate { it to setOf(req.headers(it)) }
    override val query: QueryParams = req.queryMap().toMap().mapValues { setOf(*it.value) }
}
