package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.HttpRouteSource
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSuite
import edu.byu.uapi.server.http.test.fixtures.FakeHttpRouteSource
import me.alexpanov.net.FreePortFinder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.net.InetAddress
import java.util.stream.Stream
import kotlin.system.exitProcess
import kotlin.test.assertFalse

@Suppress("FunctionName")
@Execution(ExecutionMode.CONCURRENT)
abstract class HttpViewComplianceTests<Handle : Any> {

    @TestFactory
    fun simpleRouting() = runSuite(SimpleRoutingSpecs)

    @TestFactory
    fun requestParams() = runSuite(RequestParameterSpecs)

    @TestFactory
    fun requestBody() = runSuite(RequestBodySpecs)

    @TestFactory
    fun responseHeaders() = runSuite(ResponseHeaderSpecs)

    @TestFactory
    fun responseBody() = runSuite(ResponseBodySpecs)

    @TestFactory
    fun contentNegotiation() = runSuite(ContentNegotiationSpecs)

    //<editor-fold desc="Server Start/Stop" defaultstate="collapsed">
    @AfterEach
    fun stopServers() {
        var panic = false
        handles.forEach { (name, handle) ->
            try {
                stopServer(handle)
            } catch (ex: Throwable) {
                panic = true
                System.err.println(" !!!!! Error stopping it server ${this::class.simpleName} $name! !!!!! ")
                ex.printStackTrace()
                Thread.sleep(1)
            }
        }
        if (panic) {
            System.err.println(
                "Due to server stopping failure, we're just gonna panic and exit the JVM process, just to be safe. " +
                    "Bye Bye!"
            )
            exitProcess(11111)
        }
    }

    private val handles = mutableMapOf<String, Handle>()

    private fun runSuite(suite: ComplianceSuite): Stream<DynamicNode> {
        val server = findServerAddress()
        // Let's put together and validate the tests before doing the hard work of starting the server
        val (routes, tests) = suite.build(findServerAddress())

        startServer(suite.name, server, routes)

        return tests
    }

    private fun findServerAddress(): ServerInfo {
        val addr = InetAddress.getLoopbackAddress()
        println("Searching for open port on ${addr.hostAddress}")
        val port = FreePortFinder.findFreeLocalPort(11111, addr)
        return ServerInfo(addr, port, "http://${addr.hostAddress}:$port")
    }

    private fun startServer(name: String, serverInfo: ServerInfo, routes: List<HttpRoute>) {
        println("\t-------- Starting it server for '$name' at ${serverInfo.url} --------")

        handles += (name to startServer(FakeHttpRouteSource(routes), serverInfo.address, serverInfo.port))
    }

    @Test
    fun `server starts on requested port`() {
        val serverInfo = findServerAddress()
        startServer("define test", serverInfo, emptyList())
        assertFalse(FreePortFinder.available(serverInfo.port, serverInfo.address))
    }

    abstract fun startServer(routes: HttpRouteSource, address: InetAddress, port: Int): Handle
    abstract fun stopServer(handle: Handle)
    //</editor-fold>

}

data class ServerInfo(
    val address: InetAddress,
    val port: Int,
    val url: String
)
