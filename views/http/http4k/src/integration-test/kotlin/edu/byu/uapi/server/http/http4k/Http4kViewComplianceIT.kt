package edu.byu.uapi.server.http.http4k

import edu.byu.uapi.server.http.engines.HttpRouteSource
import edu.byu.uapi.server.http.integrationtest.HttpViewComplianceTests
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer
import java.net.InetAddress
import org.http4k.routing.routes as defineRoutes

class Http4kViewComplianceIT : HttpViewComplianceTests<Http4kServer>() {
    override fun startServer(routes: HttpRouteSource, address: InetAddress, port: Int): Http4kServer {
        return defineRoutes(
            *uapi(routes),
            "/hello" bind defineRoutes(
                "/{name:.*}" bind Method.GET to { request: Request -> Response(OK).body("Hello, ${request.path("name")}!") }
            ),
            "/fail" bind Method.POST to { request: Request -> Response(INTERNAL_SERVER_ERROR) }
        ).asServer(Netty(8000)).start()
    }

    override fun stopServer(handle: Http4kServer) {
        handle.stop()
    }
}
