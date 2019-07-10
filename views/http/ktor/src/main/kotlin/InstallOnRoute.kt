package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.path.PathFormatters
import edu.byu.uapi.server.http.path.format
import io.ktor.http.HttpMethod
import io.ktor.routing.Route
import io.ktor.routing.method
import edu.byu.uapi.server.http.HttpMethod as UAPIHttpMethod

private val ktorPathFormatter = PathFormatters.FLAT_CURLY_BRACE

internal fun installOnRoute(base: Route, route: HttpRoute) {
    val path = ktorPathFormatter.format(route.pathParts)
    val method = route.method.ktor

    base.method(method) {
        base.handle {

        }
    }
}

private val UAPIHttpMethod.Routable.ktor: HttpMethod
    get() = when (this) {
        UAPIHttpMethod.Routable.GET    -> HttpMethod.Get
        UAPIHttpMethod.Routable.PUT    -> HttpMethod.Put
        UAPIHttpMethod.Routable.PATCH  -> HttpMethod.Patch
        UAPIHttpMethod.Routable.POST   -> HttpMethod.Post
        UAPIHttpMethod.Routable.DELETE -> HttpMethod.Delete
    }
