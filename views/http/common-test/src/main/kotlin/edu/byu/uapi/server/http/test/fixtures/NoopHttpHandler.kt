package edu.byu.uapi.server.http.test.fixtures

import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.HttpResponse

object NoopHttpHandler : HttpHandler {
    override suspend fun handle(request: HttpRequest): HttpResponse {
        TODO("not implemented")
    }
}
