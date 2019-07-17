package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http._internal.DefaultErrorMapper
import edu.byu.uapi.server.http._internal.HttpRouteDefinition
import edu.byu.uapi.server.http._internal.RouteSourceImpl
import edu.byu.uapi.server.http.engines.HttpRouteSource
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import me.alexpanov.net.FreePortFinder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.net.InetAddress
import java.time.Duration
import java.time.Instant
import java.util.stream.Stream
import kotlin.system.exitProcess
import kotlin.test.assertFalse

/**
 * A complete suite of tests that ensure that an HTTP engine has all of the functionality we expect it to have in
 * order to represent all valid UAPI functions.
 */
@Suppress("FunctionName", "TooManyFunctions")
@Execution(ExecutionMode.CONCURRENT)
abstract class HttpViewComplianceTests<Handle : Any> {

    /**
     * Start the HTTP server on the given [address] and [port] with the specified [routes].
     * @return Some sort of handle that can be used to shut down the server later.
     */
    abstract fun startServer(routes: HttpRouteSource, address: InetAddress, port: Int): Handle

    /**
     * Shut down the server with the given [handle].
     */
    abstract fun stopServer(handle: Handle)

    //<editor-fold desc="Test Suites" defaultstate="collapsed">

    @TestFactory
    fun simpleRouting() = runSpecs(SimpleRoutingSpecs)

    @TestFactory
    fun requestParams() = runSpecs(RequestParameterSpecs)

    @TestFactory
    fun requestBody() = runSpecs(RequestBodySpecs)

    @TestFactory
    fun responseHeaders() = runSpecs(ResponseHeaderSpecs)

    @TestFactory
    fun responseBody() = runSpecs(ResponseBodySpecs)

    @TestFactory
    fun contentNegotiation() = runSpecs(ContentNegotiationSpecs)

    @TestFactory
    fun errorHandling() = runSpecs(ErrorHandlingSpecs)

    //</editor-fold>

    //<editor-fold desc="Server Start/Stop" defaultstate="collapsed">
    @AfterEach
    fun stopServers() {
        var panic = false
        handles.forEach { (name, handle) ->
            try {
                stopServer(handle)
            } catch (ex: Throwable) {
                panic = true
                System.err.println(" !!!!! Error stopping test server ${this::class.simpleName} $name! !!!!! ")
                ex.printStackTrace()
                Thread.sleep(1)
            }
        }
        if (panic) {
            System.err.println(
                "Due to server stopping failure, we're just gonna panic and exit the JVM process, just to be safe. " +
                    "Bye Bye!"
            )
            exitProcess(1)
        }
    }

    private val handles = mutableMapOf<String, Handle>()

    private fun runSpecs(suite: ComplianceSpecSuite): Stream<DynamicNode> {
        val server = findServerAddress()
        // Let's put together and validate the tests before doing the hard work of starting the server
        val (routes, tests) = suite.build(server)

        startServer(suite.name, server, routes)

        return tests.stream()
    }

    private fun findServerAddress(): ServerInfo {
        val addr = InetAddress.getLoopbackAddress()
        println("Searching for open port on ${addr.hostAddress}")
        val port = FreePortFinder.findFreeLocalPort(11111, addr)
        return ServerInfo(addr, port, "http://${addr.hostAddress}:$port")
    }

    private fun startServer(name: String, serverInfo: ServerInfo, routes: List<HttpRouteDefinition<*>>) {
        println("\t-------- Starting it server for '$name' at ${serverInfo.url} --------")
        val start = Instant.now()

        val server = startServer(RouteSourceImpl(routes, DefaultErrorMapper), serverInfo.address, serverInfo.port)
        handles += (name to server)

        val duration = Duration.between(start, Instant.now()).toMillis().toDouble()
        println("Started server for $name in ${duration / 1000} seconds\n")
    }

    @Test
    fun `server starts on requested port`() {
        val serverInfo = findServerAddress()
        startServer("define test", serverInfo, emptyList())
        assertFalse(FreePortFinder.available(serverInfo.port, serverInfo.address))
    }
    //</editor-fold>

}

data class ServerInfo(
    val address: InetAddress,
    val port: Int,
    val url: String
)
