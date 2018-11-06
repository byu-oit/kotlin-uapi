package edu.byu.uapi.http

import java.io.Writer

interface HttpResponse {

    val status: Int
    val headers: HttpHeaders
    val body: ResponseBody

}

interface ResponseBody {

    fun asString(): String
    fun toWriter(writer: Writer) {
        writer.use { it.write(asString()) }
    }

}

object EmptyResponseBody : ResponseBody {
    override fun asString(): String {
        return ""
    }
}
