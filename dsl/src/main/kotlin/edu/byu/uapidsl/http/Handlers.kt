package edu.byu.uapidsl.http

interface HttpHandler<RequestType : HttpRequest> {
    fun handle(request: RequestType): HttpResponse
}

interface GetHandler : HttpHandler<GetRequest>

interface PostHandler : HttpHandler<PostRequest>

interface PutHandler : HttpHandler<PutRequest>

interface PatchHandler : HttpHandler<PatchRequest>

interface DeleteHandler : HttpHandler<DeleteRequest>

interface OptionsHandler : HttpHandler<OptionsRequest>

