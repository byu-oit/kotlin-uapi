package edu.byu.uapi.server.http

interface HttpHandler {
    suspend fun handle(request: HttpRequest): HttpResponse
}

