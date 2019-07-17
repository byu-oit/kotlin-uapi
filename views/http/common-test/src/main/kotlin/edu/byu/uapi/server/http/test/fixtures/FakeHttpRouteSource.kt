package edu.byu.uapi.server.http.test.fixtures

import edu.byu.uapi.server.http.engines.HttpEngine
import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.engines.HttpRouteSource
import edu.byu.uapi.server.http.errors.HttpErrorMapper

class FakeHttpRouteSource<E: Any>(
    val routes: List<HttpRoute<E>>,
    val errorMapper: HttpErrorMapper = RethrowingErrorMapper
) : HttpRouteSource {
    constructor(vararg routes: HttpRoute<E>): this(routes.toList())

    @Suppress("UNCHECKED_CAST")
    override fun <E : Any> buildRoutesFor(engine: HttpEngine<E>): List<HttpRoute<E>> {
        return routes as List<HttpRoute<E>>
    }

    override fun buildErrorMapper(): HttpErrorMapper {
        return errorMapper
    }

    //    override fun buildRoutesFor(): List<HttpRoute<*>> {
//        return routes
//    }
//
//    override fun buildErrorMapperFor(): HttpErrorMapper {
//        return errorMapper
//    }
}

