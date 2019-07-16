package edu.byu.uapi.server.http

import edu.byu.uapi.server.http.errors.HttpErrorMapper

interface HttpRouteSource {
    fun buildRoutes(): List<HttpRoute>

    fun buildErrorMapper(): HttpErrorMapper
}
