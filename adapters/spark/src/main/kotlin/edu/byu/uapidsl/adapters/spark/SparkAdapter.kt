package edu.byu.uapidsl.adapters.spark

import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.http.*
import edu.byu.uapidsl.http.path.CompoundPathVariablePart
import edu.byu.uapidsl.http.path.PathPart
import edu.byu.uapidsl.http.path.SimplePathVariablePart
import edu.byu.uapidsl.http.path.StaticPathPart
import spark.Request
import spark.Response
import spark.Route
import spark.Service
import spark.Service.ignite

fun <AuthContext : Any> UApiModel<AuthContext>.igniteSpark(port: Int = 4567): Service {
    val spark: Service = ignite()

    spark.port(port)

    val paths = this.httpPaths

    for (path in paths) {
        val pathString = stringify(path.pathParts)

        val (options, get, post, put, patch, delete) = path.handlers

        spark.options(pathString, SparkOptions(options))
        println("OPTIONS - $pathString")
        if (get != null) {
            spark.get(pathString, SparkGet(get))
            println("GET - $pathString")
        }
        if (post != null) {
            spark.post(pathString, SparkPost(post))
            println("POST - $pathString")
        }
        if (put != null) {
            spark.put(pathString, SparkPut(put))
            println("PUT - $pathString")
        }
        if (patch != null) {
            spark.patch(pathString, SparkPatch(patch))
            println("PATCH - $pathString")
        }
        if (delete != null) {
            spark.delete(pathString, SparkDelete(delete))
            println("DELETE - $pathString")
        }
    }

    println("Spark is listening on port $port")

    return spark
}

fun stringify(pathParts: List<PathPart>): String {
    return pathParts.joinToString(separator = "/", prefix = "/") { part ->
        when (part) {
            is StaticPathPart -> part.part
            is SimplePathVariablePart -> ":" + part.name
            is CompoundPathVariablePart -> part.names.joinToString(separator = ",") { ":$it" }
        }
    }
}

sealed class SparkHandler<ReqType : HttpRequest, Handler : HttpHandler<ReqType>>(private val handler: Handler) : Route {

    protected abstract fun wrapRequest(req: Request): ReqType

    final override fun handle(req: Request, res: Response): String {
        try {
            val wrappedReq = wrapRequest(req)

            val dslResponse = handler.handle(wrappedReq)

            res.status(dslResponse.status)
            dslResponse.headers.forEach { name, values -> values.forEach { res.header(name, it) } }

            return dslResponse.body.asString()
        } catch (ex: Throwable) {
            ex.printStackTrace()
            res.status(500)
            return ex.toString()
        }
    }
}

class SparkOptions(handler: OptionsHandler) : SparkHandler<OptionsRequest, OptionsHandler>(handler) {
    override fun wrapRequest(req: Request): OptionsRequest = SparkOptionsRequest(req)
}

class SparkGet(handler: GetHandler) : SparkHandler<GetRequest, GetHandler>(handler) {
    override fun wrapRequest(req: Request): GetRequest = SparkGetRequest(req)
}

class SparkPut(handler: PutHandler) : SparkHandler<PutRequest, PutHandler>(handler) {
    override fun wrapRequest(req: Request): PutRequest = SparkPutRequest(req)
}

class SparkPost(handler: PostHandler) : SparkHandler<PostRequest, PostHandler>(handler) {
    override fun wrapRequest(req: Request): PostRequest = SparkPostRequest(req)
}

class SparkPatch(handler: PatchHandler) : SparkHandler<PatchRequest, PatchHandler>(handler) {
    override fun wrapRequest(req: Request): PatchRequest = SparkPatchRequest(req)
}

class SparkDelete(handler: DeleteHandler) : SparkHandler<DeleteRequest, DeleteHandler>(handler) {
    override fun wrapRequest(req: Request): DeleteRequest = SparkDeleteRequest(req)
}

abstract class SparkRequest(req: Request) : HttpRequest {
    override val path: PathParams = SimplePathParams(req.params().mapKeys { it.key.substring(1) })
    override val headers: Headers = SimpleHeaders(
        req.headers().map { it.toLowerCase() to setOf(req.headers(it)) }.toMap()
    )
}

class SparkOptionsRequest(req: Request) : SparkRequest(req), OptionsRequest
class SparkDeleteRequest(req: Request) : SparkRequest(req), DeleteRequest
class SparkGetRequest(req: Request) : SparkRequest(req), GetRequest {
    override val query: QueryParams = SimpleQueryParams(req.queryMap().toMap().mapValues { it.value.toSet() })
}

abstract class SparkBodyRequest(req: Request) : SparkRequest(req), HttpBodyRequest {
    override val body: RequestBody = StringRequestBody(req.body())
}

class SparkPostRequest(req: Request) : SparkBodyRequest(req), PostRequest
class SparkPutRequest(req: Request) : SparkBodyRequest(req), PutRequest
class SparkPatchRequest(req: Request) : SparkBodyRequest(req), PatchRequest

