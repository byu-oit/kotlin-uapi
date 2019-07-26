package edu.byu.uapi.server.http.test.fixtures

import edu.byu.uapi.server.http._internal.HttpHandler
import edu.byu.uapi.server.http._internal.HttpRequest
import edu.byu.uapi.server.http.engines.HttpResponse

class NoopHttpHandler<R: HttpRequest> : HttpHandler<R> {
    override suspend fun handle(request: R): HttpResponse {
        TODO("not implemented")
    }
}
