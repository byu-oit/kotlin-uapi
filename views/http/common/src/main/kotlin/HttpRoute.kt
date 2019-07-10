package edu.byu.uapi.server.http

import edu.byu.uapi.server.http.path.PathPart

data class HttpRoute(
    val pathParts: List<PathPart>,
    val method: HttpMethod.Routable,
    val handler: HttpHandler,
    val consumes: String? = null,
    val produces: String? = null
)
