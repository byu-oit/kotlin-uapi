package edu.byu.uapi.server.http.test.fixtures

import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.HttpRouteSource

class FakeHttpRouteSource(
    val routes: List<HttpRoute>
) : HttpRouteSource {
    constructor(vararg routes: HttpRoute): this(routes.toList())

    override fun buildRoutes(): List<HttpRoute> {
        return routes
    }
}
