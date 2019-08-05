package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.path.PathFormatters
import io.ktor.routing.Route

private val ktorPathFormatter = PathFormatters.FLAT_CURLY_BRACE

internal fun installOnRoute(base: Route, route: HttpRoute<*>) {
    TODO()
//    val pathSpec = ktorPathFormatter.format(route.pathParts)
//    val method = route.method.ktor

//    base.method(method) {
//        base.handle {
//
//        }
//    }
}

//private val UAPIHttpMethod.Routable.ktor: HttpMethod
//    get() = when (this) {
//        UAPIHttpMethod.Routable.GET    -> HttpMethod.Get
//        UAPIHttpMethod.Routable.PUT    -> HttpMethod.Put
//        UAPIHttpMethod.Routable.PATCH  -> HttpMethod.Patch
//        UAPIHttpMethod.Routable.POST   -> HttpMethod.Post
//        UAPIHttpMethod.Routable.DELETE -> HttpMethod.Delete
//    }
