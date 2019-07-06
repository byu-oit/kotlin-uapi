package edu.byu.uapi.server.http

interface HttpRouteSource {
    fun buildRoutes(): List<HttpRoute>
}
