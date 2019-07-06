package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.HttpRoute
import io.ktor.http.HttpMethod
import io.ktor.routing.Route
import io.ktor.routing.method
import path.PathFormatters
import path.format
import edu.byu.uapi.server.http.HttpMethod as UAPIHttpMethod

private val ktorPathFormatter = PathFormatters.CURLY_BRACE

internal fun installOnRoute(base: Route, route: HttpRoute) {
    val path = ktorPathFormatter.format(route.pathParts)
    val method = route.method.ktor

    base.method(method) {
        base.handle {

        }
    }
}

private val UAPIHttpMethod.ktor: HttpMethod
    get() = when (this) {
        UAPIHttpMethod.GET    -> HttpMethod.Get
        UAPIHttpMethod.PUT    -> HttpMethod.Put
        UAPIHttpMethod.PATCH  -> HttpMethod.Patch
        UAPIHttpMethod.POST   -> HttpMethod.Post
        UAPIHttpMethod.DELETE -> HttpMethod.Delete
    }
