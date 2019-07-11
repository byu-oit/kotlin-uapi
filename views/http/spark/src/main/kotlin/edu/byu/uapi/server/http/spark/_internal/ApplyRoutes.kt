package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.HttpRouteSource
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.format
import kotlinx.coroutines.Dispatchers
import spark.Route

fun RouteApplier.applyRoutes(routes: HttpRouteSource) {
    routes.buildRoutes()
        .groupBy { RouteSpec(it) }
        .forEach { (spec, routes) ->
            when (spec.method) {
                HttpMethod.GET    -> apply(spec, routes, this::get)
                HttpMethod.PUT    -> apply(spec, routes, this::put)
                HttpMethod.PATCH  -> apply(spec, routes, this::patch)
                HttpMethod.POST   -> apply(spec, routes, this::post)
                HttpMethod.DELETE -> apply(spec, routes, this::delete)
            }
        }
}

internal val sparkCoroutines = Dispatchers.Unconfined

private inline fun apply(spec: RouteSpec, routes: List<HttpRoute>, fn: (String, String?, Route) -> Unit) {
    val adapter = if (routes.size == 1) {
        val route = routes.single()
        SimpleRouteAdapter(spec.pathParts, route.handler, sparkCoroutines)
    } else {
        ConsumesMultipleTypesRouteAdapter(
            spec.pathParts,
            routes.associate { (it.consumes ?: "*/*") to it.handler },
            sparkCoroutines
        )
    }
    fn(
        spec.path,
        spec.acceptType,
        adapter
    )
}

internal data class RouteSpec(
    val pathParts: RoutePath,
    val method: HttpMethod,
    val acceptType: String?
) {
    val path = sparkPaths.format(pathParts)

    constructor(route: HttpRoute) : this(
        pathParts = route.pathParts,
        method = route.method,
        acceptType = route.produces
    )
}
