package edu.byu.uapi.http

import edu.byu.uapi.spi.requests.Headers
import java.io.InputStream
import java.io.Reader

interface HttpRequest {
    val method: HttpMethod
    val path: HttpPathParams
    val headers: Headers
    val query: HttpQueryParams
    val rawPath: String
    val body: HttpRequestBody?
}

interface HttpRequestBody {
    fun asStream(): InputStream
    fun asReader(): Reader
    fun asString(): String
}

data class StringHttpRequestBody(val body: String): HttpRequestBody {
    override fun asStream(): InputStream {
        return body.byteInputStream()
    }

    override fun asReader(): Reader {
        return body.reader()
    }

    override fun asString(): String {
        return body
    }
}

typealias HttpPathParams = Map<String, String>
typealias HttpQueryParams = Map<String, Set<String>>
