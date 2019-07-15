package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.HttpRouteSource
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.format
import spark.Route
import kotlin.coroutines.EmptyCoroutineContext

fun RouteApplier.applyRoutes(routes: HttpRouteSource) {
    routes.buildRoutes()
        .groupBy { RouteSpec(it) }
        .forEach { (spec, routes) ->
            when (spec.method) {
                HttpMethod.Routable.GET    -> apply(spec, routes, this::get)
                HttpMethod.Routable.PUT    -> apply(spec, routes, this::put)
                HttpMethod.Routable.PATCH  -> apply(spec, routes, this::patch)
                HttpMethod.Routable.POST   -> apply(spec, routes, this::post)
                HttpMethod.Routable.DELETE -> apply(spec, routes, this::delete)
            }.apply {/* exhaustive */}
        }
}

internal val sparkCoroutines = EmptyCoroutineContext

private inline fun apply(spec: RouteSpec, routes: List<HttpRoute>, fn: (String, String?, Route) -> Unit) {
    println("Adding route ${spec.method} ${spec.path}")
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
    val method: HttpMethod.Routable,
    val acceptType: String?
) {
    val path = sparkPaths.format(pathParts)

    constructor(route: HttpRoute) : this(
        pathParts = route.pathParts,
        method = route.method,
        acceptType = route.produces
    )
}
