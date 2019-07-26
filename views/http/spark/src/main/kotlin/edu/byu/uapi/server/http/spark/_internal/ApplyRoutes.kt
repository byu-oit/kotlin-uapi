package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.engines.HttpRouteSource
import edu.byu.uapi.server.http.engines.RouteMethod
import edu.byu.uapi.server.http.engines.RouteMethod.DELETE
import edu.byu.uapi.server.http.engines.RouteMethod.GET
import edu.byu.uapi.server.http.engines.RouteMethod.PATCH
import edu.byu.uapi.server.http.engines.RouteMethod.POST
import edu.byu.uapi.server.http.engines.RouteMethod.PUT
import edu.byu.uapi.server.http.errors.HttpErrorMapper
import spark.Request

fun RouteApplier.applyRoutes(routes: HttpRouteSource) {
    val errorMapper = routes.buildErrorMapper()

    routes.buildRoutesFor(SparkEngine)
        .groupBy { RouteSpec(it) }
        .mapValues { buildAdapter(it.key, it.value, errorMapper) }
        .forEach { (k, v) -> applyToPath(k, v) }
}

private fun RouteApplier.applyToPath(
    spec: RouteSpec,
    adapter: BaseSparkRouteAdapter
) {
    val (pathSpec, method, produces) = spec
    println("Adding route $method $pathSpec produces=$produces to $adapter")
    when (method) {
        GET    -> get(pathSpec, produces, adapter)
        POST   -> post(pathSpec, produces, adapter)
        PUT    -> put(pathSpec, produces, adapter)
        PATCH  -> patch(pathSpec, produces, adapter)
        DELETE -> delete(pathSpec, produces, adapter)
    }.apply { /* exhaustive */ }
}

private data class RouteSpec(
    val pathSpec: String,
    val method: RouteMethod,
    val produces: String?
) {
    constructor(route: HttpRoute<Request>) : this(route.pathSpec, route.method, route.produces)
}

private fun buildAdapter(
    spec: RouteSpec,
    routes: List<HttpRoute<Request>>,
    errorMapper: HttpErrorMapper
): BaseSparkRouteAdapter {
    return if (spec.method.mayHaveBody) {
        HasBodyRouteAdapter(
            routes, sparkCoroutines, errorMapper
        )
    } else {
        assert(routes.size == 1)
        NoBodyRouteAdapter(routes.single(), sparkCoroutines, errorMapper)
    }
}
