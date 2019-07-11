package edu.byu.uapi.server.http.integrationtest.dsl

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.interceptors.LogRequestInterceptor
import com.github.kittinunf.fuel.core.interceptors.LogResponseInterceptor
import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.HttpResponse
import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.integrationtest.ServerInfo
import edu.byu.uapi.server.http.integrationtest.TestResponse
import edu.byu.uapi.server.http.path.CompoundVariablePathPart
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.SingleVariablePathPart
import edu.byu.uapi.server.http.path.staticPart
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.net.URI
import java.util.stream.Stream
import kotlin.test.fail

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
        return init.buildRoutes()
    }

    fun buildTests(serverInfo: ServerInfo): Stream<DynamicNode> {
        return init.buildTests(serverInfo)
    }
}

@ComplianceDsl
class ComplianceSuiteInit(private val suiteName: String) {

    private val routes = mutableListOf<HttpRoute>()

    fun routes(init: RoutingInit.() -> Unit) {
        routes += RoutingInit(emptyList()).apply(init).buildRoutes()
    }

    internal fun buildRoutes() = routes

    private lateinit var rootTestGroup: TestGroupInit

    fun tests(init: TestGroupInit.() -> Unit) {
        rootTestGroup = TestGroupInit(null).apply(init)
    }

    internal fun buildTests(serverInfo: ServerInfo): Stream<DynamicNode> {
        val uri = TestUri(suiteName)
        val http = FuelManager().apply {
            basePath = serverInfo.url
            addRequestInterceptor { LogRequestInterceptor(it) }
            addResponseInterceptor { LogResponseInterceptor(it) }
        }

        return rootTestGroup.buildTests(http, uri)
    }

    internal fun buildSuite(): ComplianceSuite {
        return ComplianceSuite(suiteName, this)
    }
}

sealed class DynamicNodeBuilder {
    internal abstract fun buildTests(http: FuelManager, parentUri: TestUri): Stream<DynamicNode>
}

class TestUri private constructor(private val parts: List<String>) {
    internal constructor(root: String) : this(listOf(root))

    fun with(part: String) = TestUri(parts + part)

    fun toUri(): URI = URI.create("compliance-dsl://" + parts.joinToString("/") { it.pathSafe })

}

@ComplianceDsl
class TestGroupInit(
    private val name: String?
) : DynamicNodeBuilder() {

    override fun buildTests(http: FuelManager, parentUri: TestUri): Stream<DynamicNode> {
        return if (name != null) {
            val uri = parentUri.with(name)
            Stream.of(
                DynamicContainer.dynamicContainer(
                    name,
                    uri.toUri(),
                    getChildTests(http, uri)
                )
            )
        } else {
            getChildTests(http, parentUri)
        }
    }

    private fun getChildTests(http: FuelManager, uri: TestUri): Stream<DynamicNode> {
        return children.stream().flatMap { it.buildTests(http, uri) }
    }

    private val children = mutableListOf<DynamicNodeBuilder>()

    fun group(name: String, init: TestGroupInit.() -> Unit) {
        children += TestGroupInit(name).apply(init)
    }

    fun test(name: String, init: TestInit.() -> Unit) {
        children += TestInit(name).apply(init)
    }

}

@ComplianceDsl
class TestInit(private val name: String) : DynamicNodeBuilder() {

    fun get(path: String, init: Request.() -> Unit) {
        request(HttpMethod.Routable.GET, path, init)
    }

    fun post(path: String, init: Request.() -> Unit) {
        request(HttpMethod.Routable.POST, path, init)
    }

    fun put(path: String, init: Request.() -> Unit) {
        request(HttpMethod.Routable.PUT, path, init)
    }

    fun patch(path: String, init: Request.() -> Unit) {
        request(HttpMethod.Routable.PATCH, path, init)
    }

    fun delete(path: String, init: Request.() -> Unit) {
        request(HttpMethod.Routable.DELETE, path, init)
    }

    fun request(method: HttpMethod, path: String, init: Request.() -> Unit) {
        requestMethod = method
        requestPath = path
        requestInit = {
            // Hacky workaround to fail an assumption on all patch requests
            Assumptions.assumeFalse(
                method == HttpMethod.Routable.PATCH,
                "Fuel doesn't currently support PATCH, so we can't test it until we swap HTTP clients in the tests."
            )
            init()
        }
    }

    fun should(init: Response.() -> Unit) {
        asserts = init
    }

    private lateinit var requestMethod: HttpMethod
    private lateinit var requestPath: String
    private lateinit var requestInit: Request.() -> Unit

    private lateinit var asserts: Response.() -> Unit

    override fun buildTests(http: FuelManager, parentUri: TestUri): Stream<DynamicNode> {
        //make sure everything's initialized
        requestMethod
        requestPath
        requestInit
        asserts

        val method = when (requestMethod) {
            HttpMethod.Routable.GET    -> Method.GET
            HttpMethod.Routable.PUT    -> Method.PUT
            HttpMethod.Routable.PATCH  -> Method.PATCH
            HttpMethod.Routable.POST   -> Method.POST
            HttpMethod.Routable.DELETE -> Method.DELETE
            HttpMethod.HEAD            -> Method.HEAD
            HttpMethod.OPTIONS         -> Method.OPTIONS
            HttpMethod.TRACE           -> Method.TRACE
            else                       -> fail("Unknown HTTP request method: $requestMethod")
        }

        return Stream.of(DynamicTest.dynamicTest(name, parentUri.with(name).toUri()) {
            val request = http.request(method, requestPath).apply(requestInit)

            val (_, response) = request.response()
            response.apply(asserts)
        })
    }
}


@ComplianceDsl
class RoutingInit(
    private val pathParts: RoutePath
) {
    private val routes = mutableListOf<HttpRoute>()

    fun path(vararg parts: String, init: RoutingInit.() -> Unit) {
        path(parts.map { staticPart(it) }, init)
    }

    fun pathParam(name: String, init: RoutingInit.() -> Unit) {
        path(listOf(SingleVariablePathPart(name)), init)
    }

    fun pathParam(vararg names: String, init: RoutingInit.() -> Unit) {
        path(listOf(CompoundVariablePathPart(names.toList())), init)
    }

    fun path(parts: RoutePath, init: RoutingInit.() -> Unit) {
        routes += RoutingInit(this.pathParts + parts).apply(init).buildRoutes()
    }

    fun get(consumes: String? = null, produces: String? = null, handler: TestHttpHandler) {
        route(HttpMethod.Routable.GET, consumes, produces, handler)
    }


    fun post(consumes: String? = null, produces: String? = null, handler: TestHttpHandler) {
        route(HttpMethod.Routable.POST, consumes, produces, handler)
    }


    fun put(consumes: String? = null, produces: String? = null, handler: TestHttpHandler) {
        route(HttpMethod.Routable.PUT, consumes, produces, handler)
    }

    fun patch(consumes: String? = null, produces: String? = null, handler: TestHttpHandler) {
        route(HttpMethod.Routable.PATCH, consumes, produces, handler)
    }

    fun delete(consumes: String? = null, produces: String? = null, handler: TestHttpHandler) {
        route(HttpMethod.Routable.DELETE, consumes, produces, handler)
    }

    private fun route(
        method: HttpMethod.Routable,
        consumes: String? = null,
        produces: String? = null,
        handler: TestHttpHandler
    ) {
        routes += HttpRoute(
            method = method,
            pathParts = pathParts,
            produces = produces,
            consumes = consumes,
            handler = CallbackHttpHandler(handler)
        )
    }

    internal fun buildRoutes(): List<HttpRoute> {
        return routes
    }
}

typealias TestHttpHandler = suspend HttpRequest.() -> TestResponse

private class CallbackHttpHandler(val handler: TestHttpHandler) : HttpHandler {
    override suspend fun handle(request: HttpRequest): HttpResponse {
        return handler(request)
    }
}

internal val String.pathSafe: String
    get() = this.replace("""[^-_0-9a-zA-Z]+""".toRegex(), "_")
