package edu.byu.uapi.server.http

import edu.byu.uapi.server.http.errors.UAPIHttpInternalError

interface HttpRequest {
    /**
     * Gets the HTTP headers from the request. Header keys should be normalized to lower-case strings.
     */
    val headers: Map<String, String>
    /**
     * Get the query parameters from the request, or an empty map if there are none.
     */
    val queryParams: Map<String, List<String>>
    /**
     * Gets the path parameters from the request.
     */
    val pathParams: Map<String, String>

    //TODO: figure out how to represent the body
}
