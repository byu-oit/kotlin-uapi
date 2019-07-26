package edu.byu.uapi.server.http._internal

import edu.byu.uapi.server.http.engines.HttpResponse

interface HttpHandler<R : HttpRequest> {
    suspend fun handle(request: R): HttpResponse
}

typealias GetHandler = HttpHandler<GetRequest>
typealias PostHandler = HttpHandler<PostRequest>
typealias PutHandler = HttpHandler<PutRequest>
typealias PatchHandler = HttpHandler<PatchRequest>
typealias DeleteHandler = HttpHandler<DeleteRequest>

