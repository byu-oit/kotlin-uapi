package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.HttpRouteSource
import edu.byu.uapi.server.http.errors.HttpErrorMapper
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.format
import spark.Route
import kotlin.coroutines.EmptyCoroutineContext

fun RouteApplier.applyRoutes(routes: HttpRouteSource) {
    val errorMapper = routes.buildErrorMapper()
    routes.buildRoutes()
        .groupBy { RouteSpec(it) }
        .forEach { (spec, routes) ->
            when (spec.method) {
                HttpMethod.Routable.GET    -> apply(spec, routes, errorMapper, this::get)
                HttpMethod.Routable.PUT    -> apply(spec, routes, errorMapper, this::put)
                HttpMethod.Routable.PATCH  -> apply(spec, routes, errorMapper, this::patch)
                HttpMethod.Routable.POST   -> apply(spec, routes, errorMapper, this::post)
                HttpMethod.Routable.DELETE -> apply(spec, routes, errorMapper, this::delete)
            }.apply {/* exhaustive */}
        }
}

internal val sparkCoroutines = EmptyCoroutineContext

private inline fun apply(
    spec: RouteSpec,
    routes: List<HttpRoute>,
    errorMapper: HttpErrorMapper,
    fn: (String, String?, Route) -> Unit
) {
    println("Adding route ${spec.method} ${spec.path}")
    val adapter = if (routes.size == 1) {
        val route = routes.single()
        SimpleRouteAdapter(spec.pathParts, route.handler, sparkCoroutines, errorMapper)
    } else {
        ConsumesMultipleTypesRouteAdapter(
            spec.pathParts,
            routes.associate { (it.consumes ?: "*/*") to it.handler },
            sparkCoroutines,
            errorMapper
        )
    }
    fn(
        spec.path,
        spec.produces,
        adapter
    )
}

private data class RouteSpec(
    val pathParts: RoutePath,
    val method: HttpMethod.Routable,
    val produces: String?
) {
    val path = sparkPaths.format(pathParts)

    constructor(route: HttpRoute) : this(
        pathParts = route.pathParts,
        method = route.method,
        produces = route.produces
    )
}
