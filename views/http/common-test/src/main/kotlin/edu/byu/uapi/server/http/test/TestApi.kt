package edu.byu.uapi.server.http.test

import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.HttpResponse
import edu.byu.uapi.server.http.HttpResponseBody
import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.HttpRouteSource
import edu.byu.uapi.server.http.path.staticPart
import edu.byu.uapi.server.http.path.variablePart
import java.io.OutputStream

fun getTestApi(): HttpRouteSource {
    // suuuper great it API (sufficient for testing for now)
    return object : HttpRouteSource {
        override fun buildRoutes(): List<HttpRoute> {
            return listOf(
                HttpRoute(
                    listOf(
                        staticPart("foo"),
                        variablePart("bar", "baz")
                    ),
                    HttpMethod.GET,
                    object : HttpHandler {
                        override suspend fun handle(request: HttpRequest): HttpResponse {
                            return object : HttpResponse {
                                override val status: Int = 200
                                override val headers: Map<String, String> = emptyMap()
                                override val body: HttpResponseBody? = object : HttpResponseBody {
                                    override val contentType: String = "text/plain"
                                    override fun writeTo(stream: OutputStream) {
                                        stream.bufferedWriter().use {
                                            it.append(request.pathParams.toString())
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            )
        }
    }
}
