package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.HttpRouteSource
import io.ktor.routing.Route

fun Route.uapi(routes: HttpRouteSource) {
    routes.buildRoutes().forEach {
        installOnRoute(this, it)
    }
}


