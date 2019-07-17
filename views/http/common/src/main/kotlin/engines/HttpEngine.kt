package edu.byu.uapi.server.http.engines

import edu.byu.uapi.server.http.path.PathFormatter

/**
 * Defines the unique properties of an HTTP Engine. This interface can usually be implemented as an `object` type,
 * as it shouldn't normally change.
 * 
 * @param[R] the request object type used by this engine. HttpServletRequest, for example.
 */
interface HttpEngine<R> {
    /**
     * The engine's name.
     */
    val engineName: String

    /**
     * How do we extract values from this engine's requests?
     */
    val requestReader: RequestReader<R>

    /**
     * How do we format path parameters so that this engine can understand them?
     */
    val pathFormatter: PathFormatter
}
