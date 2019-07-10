package edu.byu.uapi.server.http

import java.io.OutputStream

interface HttpResponse {
    val status: Int
    val headers: Map<String, String>
    val body: HttpResponseBody?
}

interface HttpResponseBody {
    val contentType: String
    fun writeTo(stream: OutputStream)
}
