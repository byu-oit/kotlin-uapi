package edu.byu.uapi.server.http.spark

import edu.byu.uapi.server.http.engines.HttpRouteSource
import edu.byu.uapi.server.http.integrationtest.HttpViewComplianceTests
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import spark.Service
import java.net.InetAddress

@Execution(ExecutionMode.CONCURRENT)
internal class SparkViewComplianceIT : HttpViewComplianceTests<Service>() {

    override fun startServer(routes: HttpRouteSource, address: InetAddress, port: Int): Service {
        return Service.ignite().apply {
            ipAddress(address.hostAddress)
            port(port)
            uapi(routes)
            init()
            awaitInitialization()
        }
    }

    override fun stopServer(handle: Service) {
        handle.stop()
        handle.awaitStop()
    }
}
