package edu.byu.uapi.server.http.integrationtest.dsl

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.RequestFactory
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.interceptors.LogRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.LogResponseInterceptor
import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.integrationtest.ServerInfo
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.StaticPathPart
import edu.byu.uapi.server.http.path.staticPart
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.util.stream.Stream

@DslMarker
annotation class ComplianceDsl

fun suite(suiteName: String, init: ComplianceSuiteInit.() -> Unit): ComplianceSuite {
    return ComplianceSuiteInit(suiteName).apply(init).buildSuite()
}

class ComplianceSuite(
    val name: String,
    private val init: ComplianceSuiteInit
) {
    fun buildRoutes(): List<HttpRoute> {
        return init.buildRoutes(emptyList())
    }

    fun buildTests(serverInfo: ServerInfo): Stream<DynamicNode> {
        return init.buildTests(serverInfo)
    }
}

class ComplianceSuiteInit(private val suiteName: String) : TestGroupInit(suiteName, null) {
    override val pathContext: List<String> = emptyList()

    override fun buildTests(serverInfo: ServerInfo): Stream<DynamicNode> {
        return super.getChildTests(serverInfo)
    }

    internal fun buildSuite(): ComplianceSuite {
        return ComplianceSuite(suiteName, this)
    }
}

@ComplianceDsl
sealed class DynamicNodeBuilder(
    internal val name: String,
    private val parent: DynamicNodeBuilder?
) {
    var disabled = false

    fun isDisabled(): Boolean = disabled || (parent != null && parent.isDisabled())

    private val pathName: String = name.toPathSegment()
    internal open val pathContext: List<String>
        get() = parent?.pathContext?.plus(pathName) ?: listOf(pathName)

    protected val routeInit: RoutingInit = RoutingInit(emptyList())

    fun givenRoutes(init: RoutingInit.() -> Unit) {
        routeInit.apply(init)
    }

    internal abstract fun buildRoutes(
        extraRoutes: List<RoutingInit>
    ): List<HttpRoute>

    internal abstract fun buildTests(
        serverInfo: ServerInfo
    ): Stream<DynamicNode>
}

open class TestGroupInit(
    name: String,
    parent: DynamicNodeBuilder?
) : DynamicNodeBuilder(name, parent) {

    fun describe(name: String, init: TestGroupInit.() -> Unit) {
        children += TestGroupInit(name, this).apply(init)
    }

    fun it(name: String, init: TestInit.() -> Unit) {
        children += TestInit(name, this).apply(init)
    }

    override fun buildRoutes(
        extraRoutes: List<RoutingInit>
    ): List<HttpRoute> {
        return children.flatMap { it.buildRoutes(extraRoutes + this.routeInit) }
    }

    override fun buildTests(
        serverInfo: ServerInfo
    ): Stream<DynamicNode> {
        return Stream.of(
            DynamicContainer.dynamicContainer(
                name,
                getChildTests(serverInfo)
            )
        )
    }

    protected fun getChildTests(serverInfo: ServerInfo): Stream<DynamicNode> {
        return children.stream().flatMap { it.buildTests(serverInfo) }
    }

    private val children = mutableListOf<DynamicNodeBuilder>()

}

typealias WhenCalledWith = RequestFactory.Convenience.() -> Request

class TestInit(name: String, parent: TestGroupInit) : DynamicNodeBuilder(name, parent) {

    fun whenCalledWith(init: WhenCalledWith) {
        requestInit = init
    }

    fun then(init: Response.() -> Unit) {
        asserts = init
    }

    private lateinit var requestInit: WhenCalledWith

    private lateinit var asserts: Response.() -> Unit

    override fun buildRoutes(extraRoutes: List<RoutingInit>): List<HttpRoute> {
        if (isDisabled()) {
            return emptyList()
        }
        val basePath = pathContext.map(::staticPart)
        return (extraRoutes + routeInit).flatMap { it.buildRoutes(basePath) }
    }

    override fun buildTests(
        serverInfo: ServerInfo
    ): Stream<DynamicNode> {
        //make sure everything's initialized
        requestInit
        asserts

        val path = pathContext.joinToString("/", prefix = "/")
        val url = serverInfo.url + path

        return Stream.of(DynamicTest.dynamicTest(name) {
            Assumptions.assumeFalse(isDisabled(), "Test is disabled")
            val request = FuelManager().apply {
                basePath = url
                addRequestInterceptor { LogRequestInterceptor(it) }
                addResponseInterceptor { LogResponseInterceptor(it) }
            }.run(requestInit)

            Assumptions.assumeFalse(
                request.method == Method.PATCH,
                "Fuel doesn't support PATCH, so we'll skip it until we can work around it."
            )

            val (_, response) = request.response()
            response.apply(asserts)
        })
    }
}

@ComplianceDsl
class RoutingInit(
    private val pathParts: RoutePath
) {
    private val routes = mutableListOf<RouteBuilding>()

    fun path(parts: RoutePath, init: RoutingInit.() -> Unit) {
        routes += RoutingInit(this.pathParts + parts).apply(init).routes
    }

    internal fun route(
        method: HttpMethod.Routable,
        consumes: String? = null,
        produces: String? = null,
        handler: TestHttpHandler
    ) {
        routes += RouteBuilding(
            path = pathParts,
            method = method,
            produces = produces,
            consumes = consumes,
            handler = handler
        )
    }

    private class RouteBuilding(
        val path: RoutePath,
        val method: HttpMethod.Routable,
        val consumes: String? = null,
        val produces: String? = null,
        val handler: TestHttpHandler
    )

    internal fun buildRoutes(basePath: List<StaticPathPart>): List<HttpRoute> {
        val basePathString = basePath.joinToString("/", prefix = "/") { it.part }
        return routes.map {
            HttpRoute(
                pathParts = basePath + it.path,
                method = it.method,
                consumes = it.consumes,
                produces = it.produces,
                handler = TestHandlerWrapper(basePathString, it.handler)
            )
        }
    }
}

typealias TestHttpHandler = suspend HttpRequest.() -> TestResponse

// Normalize and shorten path names
internal fun String.toPathSegment(maxLength: Int = 30): String {
    return this.toLowerCase()
        .replace("'", "")
        .replace("""(to|the|a) """.toRegex(), "")
        .replace("""[^-_0-9a-z]+""".toRegex(), "_")
        .take(maxLength)
}
