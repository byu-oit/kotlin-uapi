package edu.byu.uapi.server.http

interface HttpHandler {
    fun handle(request: HttpRequest): HttpResponse
}

