package edu.byu.uapi.server.http

import edu.byu.uapi.server.http.path.PathPart

data class HttpRoute(
    val pathParts: List<PathPart>,
    val method: HttpMethod,
    val handler: HttpHandler,
    val consumes: List<String> = emptyList(),
    val produces: List<String> = emptyList()
)
