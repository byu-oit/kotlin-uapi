package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.HttpRouteSource
import edu.byu.uapi.server.http.integrationtest.HttpViewComplianceTests
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.junit.jupiter.api.Disabled
import java.net.InetAddress
import java.util.concurrent.TimeUnit

@Disabled("Ktor server has not been implemented yet")
internal class KtorViewComplianceIT : HttpViewComplianceTests<ApplicationEngine>() {

    override fun startServer(routes: HttpRouteSource, address: InetAddress, port: Int): ApplicationEngine {
        return embeddedServer(
            Netty,
            host = address.hostAddress,
            port = port
        ) {
            routing {
                uapi(routes)
            }
        }
    }

    override fun stopServer(handle: ApplicationEngine) {
        handle.stop(0, 2, TimeUnit.SECONDS)
    }

}
