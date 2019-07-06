package edu.byu.uapi.server.http

interface HttpController {
    fun buildRoutes(): List<HttpRoute>
}
