package edu.byu.uapi.server.http.http4k

import edu.byu.uapi.server.http.engines.HttpRouteSource
import edu.byu.uapi.server.http.engines.RouteMethod
import edu.byu.uapi.server.http.http4k._internal.Http4kEngine
import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind

fun uapi(routes: HttpRouteSource): Array<RoutingHttpHandler> {
    val list = routes.buildRoutesFor(Http4kEngine)
    list.map { route ->
        route.pathSpec bind route.method.http4k //to handler()
    }
    TODO()
}

private val RouteMethod.http4k: Method
    get() = when(this) {
        RouteMethod.GET    -> Method.GET
        RouteMethod.POST   -> Method.POST
        RouteMethod.PUT    -> Method.PUT
        RouteMethod.PATCH  -> Method.PATCH
        RouteMethod.DELETE -> Method.DELETE
    }
