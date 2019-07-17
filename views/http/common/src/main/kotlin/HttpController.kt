package edu.byu.uapi.server.http

import edu.byu.uapi.server.http.engines.HttpRoute

interface HttpController {
    fun buildRoutes(): List<HttpRoute<*>>
}
