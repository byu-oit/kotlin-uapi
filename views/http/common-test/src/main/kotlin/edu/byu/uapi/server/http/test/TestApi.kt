package edu.byu.uapi.server.http.test

import edu.byu.uapi.server.http._internal.GetHandler
import edu.byu.uapi.server.http._internal.GetRequest
import edu.byu.uapi.server.http._internal.GetRoute
import edu.byu.uapi.server.http.engines.HttpResponse
import edu.byu.uapi.server.http.engines.HttpResponseBody
import edu.byu.uapi.server.http.engines.HttpRouteSource
import edu.byu.uapi.server.http._internal.RouteSourceImpl
import edu.byu.uapi.server.http.path.staticPart
import edu.byu.uapi.server.http.path.variablePart
import java.io.OutputStream

private val getRoute =
    GetRoute(
        listOf(
            staticPart("foo"),
            variablePart("bar", "baz")
        ),
        object : GetHandler {
            override suspend fun handle(request: GetRequest): HttpResponse {
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

fun getTestApi(): HttpRouteSource {
    // suuuper great it API (sufficient for testing for now)
    return RouteSourceImpl(listOf(getRoute))
}
