package edu.byu.uapi.http

interface HttpHandler {
    fun handle(request: HttpRequest): HttpResponse
}

