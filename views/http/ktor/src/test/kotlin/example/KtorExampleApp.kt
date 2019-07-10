package edu.byu.uapi.server.http.ktor.example

import edu.byu.uapi.server.http.ktor.uapi
import edu.byu.uapi.server.http.test.getTestApi
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication

fun main() {
    withTestApplication({
        routing {
            get("/") {
                call.respondText("Hello, world!", ContentType.Text.Html)
            }
            route("/api") {
                uapi(getTestApi())
            }
        }
    }) {
        handleRequest(io.ktor.http.HttpMethod.Get, "/compound/123,456").apply {
            println(response.status())
            println(response.content)
        }
    }
}

