package edu.byu.uapi.server.http.spark

import edu.byu.uapi.server.http.HttpRouteSource
import edu.byu.uapi.server.http.integrationtest.HttpViewComplianceIntegrationTestBase
import spark.Service
import java.net.InetAddress

internal class SparkViewComplianceIT : HttpViewComplianceIntegrationTestBase<Service>() {
    override fun startServer(routes: HttpRouteSource, address: InetAddress, port: Int): Service {
        val service = Service.ignite()
        service.ipAddress(address.hostAddress)
        service.port(port)
        service.uapi(routes)
        service.awaitInitialization()
        return service
    }

    override fun stopServer(handle: Service) {
        handle.stop()
        handle.awaitStop()
    }
}
