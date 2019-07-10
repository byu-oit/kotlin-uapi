package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.HttpRouteSource
import edu.byu.uapi.server.http.test.getTestApi
import me.alexpanov.net.FreePortFinder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetAddress

abstract class HttpViewComplianceIntegrationTestBase<Handle: Any> {

    abstract fun startServer(routes: HttpRouteSource, address: InetAddress, port: Int): Handle
    abstract fun stopServer(handle: Handle)

    private lateinit var baseUrl: String
    private lateinit var serverHandle: Handle

    @BeforeEach
    fun doStartServer() {
        val addr = InetAddress.getLocalHost()
        val port = FreePortFinder.findFreeLocalPort(addr)

        baseUrl = "http://${addr.hostAddress}:$port"
        println("\t-------- Starting test server at $baseUrl --------")
        serverHandle = startServer(getTestApi(), addr, port)
    }

    @AfterEach
    fun doStopServer() {
        if (this::serverHandle.isInitialized) {
            stopServer(serverHandle)
        }
    }

    @Test
    fun `Server starts on requested port`() {
        println("hi")
    }

}
