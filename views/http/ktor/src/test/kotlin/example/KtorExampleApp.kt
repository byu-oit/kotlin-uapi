package edu.byu.uapi.server.http.ktor.example

import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.HttpResponse
import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.HttpRouteSource
import edu.byu.uapi.server.http.ResponseBody
import edu.byu.uapi.server.http.ktor.uapi
import edu.byu.uapi.server.http.path.staticPart
import edu.byu.uapi.server.http.path.variablePart
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import java.io.OutputStream

fun main() {
    withTestApplication({
        routing {
            get("/") {
                call.respondText("Hello, world!", ContentType.Text.Html)
            }
            get("/compound/{bar},{baz}") {
                call.respondText(call.parameters.toString())
            }
            route("/api") {
                uapi(object : HttpRouteSource {
                    override fun buildRoutes(): List<HttpRoute> {
                        return listOf(
                            HttpRoute(
                                listOf(
                                    staticPart("foo"),
                                    variablePart("bar", "baz")
                                ),
                                HttpMethod.GET,
                                object : HttpHandler {
                                    override fun handle(request: HttpRequest): HttpResponse {
                                        return object : HttpResponse {
                                            override val status: Int = 200
                                            override val headers: Map<String, String> = emptyMap()
                                            override val responseBody: ResponseBody? = object : ResponseBody {
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
                })
            }
        }
    }) {
        handleRequest(io.ktor.http.HttpMethod.Get, "/compound/123,456").apply {
            println(response.status())
            println(response.content)
        }
//        handleRequest(io.ktor.http.HttpMethod.Get, "/api/foo/123/456").apply {
//            println(response.status())
//            println(response.content)
//        }
    }
}

