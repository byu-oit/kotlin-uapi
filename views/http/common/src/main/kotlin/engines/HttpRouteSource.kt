package edu.byu.uapi.server.http.engines

import edu.byu.uapi.server.http.errors.HttpErrorMapper

/**
 * A source for routes.
 *
 * Engine implementations should define some way to map one of these into their own routing engines.
 */
interface HttpRouteSource {
    /**
     * Given an engine definition, gets the HTTP routes that engine needs to define.
     * @param[E] the type of request object the engine uses
     * @param[engine] the engine definition.
     * @return All routes for the UAPI model.
     */
    fun <E: Any> buildRoutesFor(engine: HttpEngine<E>): List<HttpRoute<E>>

    /**
     * Returns the error mapper that the routing engine should use to map any errors coming out of route invocations.
     */
    fun buildErrorMapper(): HttpErrorMapper
}
