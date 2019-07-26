package edu.byu.uapi.server.http.engines

/**
 * An abstract HTTP Routing target.
 *
 * An engine implementation should be able to map a list of these into its routing mechanism. To process a request,
 * call the 'dispatch' method with the request type your engine uses.
 *
 * @param[EngineRequest] the type of request the engine uses.
 * @see[HttpEngine]
 */
interface HttpRoute<EngineRequest : Any> {

    /**
     * What HTTP method to map this to
     */
    val method: RouteMethod

    /**
     * The formatted path string to call this route. Always begins with `/`, and has all path variables included,
     * according to the engine's formatting rules.
     *
     * @see[HttpEngine.pathFormatter]
     */
    val pathSpec: String

    /**
     * The MIME type, possibly with wildcards, that this route knows how to consume. Always null for GET and DELETE
     * routes, and should be assumed to be '* / *' or unspecified if not set.
     *
     * This should be used, together with a request's `Content-Type` header, to determine which route to call, using
     * normal MIME type parsing/comparison.
     */
    val consumes: String?

    /**
     * The MIME type, possibly with wildcards, that this route knows how to produce. If null, '* / *' should be assumed.
     * This should be used, together with a reqeust's `Accept` header, to determine which route to call, using
     * normal MIME type parsing/comparison.
     */
    val produces: String?

    /**
     * Invoke this route. The response should be used to generate the actual response, according to the mandates of the
     * underlying HTTP framework.
     *
     * Calls to this method should normally be wrapped with [edu.byu.uapi.server.http.errors.HttpErrorMapper.runHandlingErrors],
     * so that all errors get mapped properly.
     *
     * @param[request] the raw framework request object.
     * @return The HTTP response
     */
    suspend fun dispatch(request: EngineRequest): HttpResponse

}
