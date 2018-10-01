package edu.byu.uapi.http.spark

import edu.byu.uapi.http.*
import edu.byu.uapi.server.UAPIRuntime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.*

private val LOG: Logger = LoggerFactory.getLogger("edu.byu.uapi.http.spark.StartSpark")

fun <UserContext: Any> UAPIRuntime<UserContext>.startSpark(
    port: Int = 4567
): UAPISparkServer<UserContext> {
    val resources = this.resources().map {
        HttpIdentifiedResource(this, it.value)
    }
    val routes = resources.flatMap { it -> it.routes }

    val spark = Service.ignite()

    spark.port(port)

    routes.forEach {
        spark.addRoute(it.method.toSpark(), it.toSpark())
    }

    LOG.info("UAPI-Spark is listening on port {}", port)

    return UAPISparkServer(port, resources, spark)
}

private fun HttpRoute.toSpark(): RouteImpl {
    val path = pathParts.stringify(PathParamDecorators.COLON)
    return RouteImpl.create(path, this.handler.toSpark())
}

private fun HttpHandler.toSpark() = SparkHttpRoute(this)

class SparkHttpRoute(val handler: HttpHandler): Route {
    override fun handle(
        request: Request,
        response: Response
    ): String {
        val resp = handler.handle(SparkRequest(request))
        response.type("application/json")
        return resp.body.asString()
    }
}

private fun HttpMethod.toSpark(): spark.route.HttpMethod {
    return when (this) {
        HttpMethod.GET -> spark.route.HttpMethod.get
        HttpMethod.PUT -> spark.route.HttpMethod.put
        HttpMethod.PATCH -> spark.route.HttpMethod.patch
        HttpMethod.POST -> spark.route.HttpMethod.post
        HttpMethod.DELETE -> spark.route.HttpMethod.delete
    }
}

data class UAPISparkServer<UserContext: Any>(
    val port: Int,
    val resources: List<HttpIdentifiedResource<UserContext, *, *>>,
    val server: Service
)
