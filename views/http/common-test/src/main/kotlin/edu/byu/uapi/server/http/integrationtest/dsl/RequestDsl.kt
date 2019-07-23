package edu.byu.uapi.server.http.integrationtest.dsl

import edu.byu.uapi.server.http.engines.RouteMethod
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class WhenCalledWithInit {

    fun get(path: String = "") = request(RouteMethod.GET, path)
    fun post(path: String = "") = request(RouteMethod.POST, path)
    fun put(path: String = "") = request(RouteMethod.PUT, path)
    fun patch(path: String = "") = request(RouteMethod.PATCH, path)
    fun delete(path: String = "") = request(RouteMethod.DELETE, path)

    fun request(method: RouteMethod, path: String = ""): RequestInit {
        val p =
            if (path.isNotEmpty() && !path.startsWith("/") && !path.startsWith("?")) {
                "/$path"
            } else {
                path
            }
        return RequestInit(method, p)
    }
}

class RequestInit internal constructor(
    private val method: RouteMethod,
    private val path: String
) {
    private val headers = mutableListOf<Pair<String, String>>()

    fun header(name: String, value: String) = header(name to value)

    fun header(vararg values: Pair<String, String>): RequestInit {
        headers += values
        return this
    }

    private var type: String = "text/plain"

    fun type(value: String): RequestInit {
        type = value
        return this
    }

    fun accept(value: String) = header("Accept", value)

    private lateinit var bodyBytes: ByteArray

    fun body(bytes: ByteArray): RequestInit {
        bodyBytes = bytes
        return this
    }

    fun body(body: String) = body(body.toByteArray())

    internal fun build(basePath: String): Request {
        return Request.Builder().apply {
            url(basePath + path)
            val body = when {
                ::bodyBytes.isInitialized -> bodyBytes.toRequestBody(type.toMediaType())
                method.mayHaveBody        -> ByteArray(0).toRequestBody(type.toMediaType())
                else                      -> null
            }
            method(method.name, body)

            headers.forEach { (k, v) -> header(k, v) }
        }.build()
    }
}

