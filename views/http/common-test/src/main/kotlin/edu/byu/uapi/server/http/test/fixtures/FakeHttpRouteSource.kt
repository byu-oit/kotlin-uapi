package edu.byu.uapi.server.http.test.fixtures

import edu.byu.uapi.server.http.errors.HttpErrorMapper
import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.HttpRouteSource

class FakeHttpRouteSource(
    val routes: List<HttpRoute>,
    val errorMapper: HttpErrorMapper = RethrowingErrorMapper
) : HttpRouteSource {
    constructor(vararg routes: HttpRoute): this(routes.toList())

    override fun buildRoutes(): List<HttpRoute> {
        return routes
    }

    override fun buildErrorMapper(): HttpErrorMapper {
        return errorMapper
    }
}

