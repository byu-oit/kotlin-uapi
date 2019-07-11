package edu.byu.uapi.server.http

import edu.byu.uapi.server.http.path.RoutePath

data class HttpRoute(
    val pathParts: RoutePath,
    val method: HttpMethod.Routable,
    val handler: HttpHandler,
    val consumes: String? = null,
    val produces: String? = null
)
