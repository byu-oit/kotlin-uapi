package edu.byu.uapi.server.http._internal

import edu.byu.uapi.server.http.engines.HttpEngine
import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.engines.HttpRouteSource
import edu.byu.uapi.server.http.errors.HttpErrorMapper

class RouteSourceImpl(
    private val definitions: List<HttpRouteDefinition<*>>,
    private val errorMapper: HttpErrorMapper = DefaultErrorMapper
): HttpRouteSource {
    override fun <E : Any> buildRoutesFor(engine: HttpEngine<E>): List<HttpRoute<E>> {
        return definitions.map { it.buildRoute(engine) }
    }

    override fun buildErrorMapper(): HttpErrorMapper {
        return errorMapper
    }
}
