package edu.byu.uapi.server.http

import java.io.OutputStream

interface HttpResponse {
    val status: Int
    val headers: Map<String, String>
    val responseBody: ResponseBody?
}

interface ResponseBody {
    val contentType: String
    fun writeTo(stream: OutputStream)
}
