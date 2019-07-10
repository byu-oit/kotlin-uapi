package edu.byu.uapi.server.http._internal

import edu.byu.uapi.server.http.HttpResponse
import edu.byu.uapi.server.http.HttpResponseBody

class HttpResponseImpl: HttpResponse {
    override val status: Int
        get() = TODO("not implemented")
    override val headers: Map<String, String>
        get() = TODO("not implemented")
    override val body: HttpResponseBody?
        get() = TODO("not implemented")
}
