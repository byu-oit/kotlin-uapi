package edu.byu.uapi.server.http.integrationtest.dsl

import edu.byu.uapi.server.http._internal.DeleteRequest
import edu.byu.uapi.server.http._internal.DeleteRoute
import edu.byu.uapi.server.http._internal.GetRequest
import edu.byu.uapi.server.http._internal.GetRoute
import edu.byu.uapi.server.http._internal.HttpRouteDefinition
import edu.byu.uapi.server.http._internal.PatchRequest
import edu.byu.uapi.server.http._internal.PatchRoute
import edu.byu.uapi.server.http._internal.PostRequest
import edu.byu.uapi.server.http._internal.PostRoute
import edu.byu.uapi.server.http._internal.PutRequest
import edu.byu.uapi.server.http._internal.PutRoute
import edu.byu.uapi.server.http.integrationtest.ServerInfo
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.StaticPathPart
import edu.byu.uapi.server.http.path.staticPart
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.platform.engine.support.descriptor.MethodSource
import java.net.URI
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaMethod

@DslMarker
annotation class ComplianceDsl

abstract class ComplianceSpecSuite {
    protected abstract fun SuiteDsl.define()

    open val name: String
        get() = this::class.simpleName?.fromUpperCamelToWords()
            ?: throw IllegalStateException("Unable to get kotlin class name")

    fun build(serverInfo: ServerInfo): Pair<List<HttpRouteDefinition<*>>, List<DynamicNode>> {
        val definition = SuiteDsl(name, methodUri).apply { define() }

        val routes = definition.buildRoutes(emptyList())

        return routes to listOf(definition.buildTests(serverInfo))
    }

    private val methodUri: URI by lazy {
        val method = this::class.memberExtensionFunctions.first {
            it.extensionReceiverParameter!!.type == SuiteDsl::class.starProjectedType
                && it.name == "define"
        }
        MethodSource.from(method.javaMethod).run {
            URI(
                "method", className,
                "$methodName($methodParameterTypes)"
            )
        }
    }
}

private fun String.fromUpperCamelToWords(): String {
    return fold(StringBuilder()) { sb, c ->
        if (c.isUpperCase() && sb.isNotEmpty() && !sb.last().isUpperCase()) {
            sb.append(' ')
        }
        sb.append(c)
    }.toString()
}

class SuiteDsl(suiteName: String, uri: URI?) : TestGroupInit(suiteName, null, uri) {
    override val pathContext: List<String> = emptyList()
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

    fun givenRoutes(pathSpec: String = "", init: RoutingInit.() -> Unit) {
        if (pathSpec.isNotEmpty()) {
            routeInit.pathSpec(pathSpec, init)
        } else {
            routeInit.apply(init)
        }
    }

    internal abstract fun buildRoutes(
        extraRoutes: List<RoutingInit>
    ): List<HttpRouteDefinition<*>>

    internal abstract fun buildTests(
        serverInfo: ServerInfo
    ): DynamicNode
}

fun TestGroupInit.describe(name: String) {
    this.describe(name) { disabled = true }
}

fun TestGroupInit.it(name: String) {
    this.it(name) { disabled = true }
}

open class TestGroupInit(
    name: String,
    parent: DynamicNodeBuilder?,
    private val uri: URI?
) : DynamicNodeBuilder(name, parent) {

    fun describe(name: String, init: TestGroupInit.() -> Unit) {
        children += TestGroupInit(name, this, null).apply(init)
    }

    fun it(name: String, init: TestInit.() -> Unit) {
        children += TestInit(name, this).apply(init)
    }

    override fun buildRoutes(
        extraRoutes: List<RoutingInit>
    ): List<HttpRouteDefinition<*>> {
        return children.flatMap { it.buildRoutes(extraRoutes + this.routeInit) }
    }

    override fun buildTests(
        serverInfo: ServerInfo
    ): DynamicNode {
        return if (children.isNotEmpty()) {
            DynamicContainer.dynamicContainer(name, uri, children.stream().map { it.buildTests(serverInfo) })
        } else {
            DynamicTest.dynamicTest(this.name, uri) { assumeFalse(true, "No tests have been defined") }
        }
    }

    private val children = mutableListOf<DynamicNodeBuilder>()

}

private val httpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            println(message)
        }
    }).apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()


class TestInit(name: String, parent: TestGroupInit) : DynamicNodeBuilder(name, parent) {

    fun whenCalledWith(init: WhenCalledWithInit.() -> RequestInit) {
        requestInit = WhenCalledWithInit().init()
    }

    fun then(init: okhttp3.Response.() -> Unit) {
        asserts = init
    }

    private lateinit var requestInit: RequestInit

    private lateinit var asserts: okhttp3.Response.() -> Unit

    override fun buildRoutes(extraRoutes: List<RoutingInit>): List<HttpRouteDefinition<*>> {
        if (isDisabled()) {
            return emptyList()
        }
        val basePath = pathContext.map(::staticPart)
        return (extraRoutes + routeInit).flatMap { it.buildRoutes(basePath) }
    }

    override fun buildTests(
        serverInfo: ServerInfo
    ): DynamicNode {
        val path = pathContext.joinToString("/", prefix = "/")
        val url = serverInfo.url + path

        return DynamicTest.dynamicTest(name) {
            //make sure everything's initialized
            assumeTrue(
                ::requestInit.isInitialized,
                "You must define a whenCalledWith{} block to build the test request!"
            )
            assumeTrue(::asserts.isInitialized, "You must define a then{} block with test assertions!")

            assumeFalse(isDisabled(), "Test is disabled")

            println()
            val request = requestInit.build(url)

            val resp = httpClient.newCall(request)
                .execute()

            resp.apply(asserts)
        }
    }
}

private typealias RouteCreator = (basePath: String, pathSpec: RoutePath) -> HttpRouteDefinition<*>

@ComplianceDsl
class RoutingInit(
    private val pathParts: RoutePath
) {
    private val routes = mutableListOf<RouteCreator>()
    private val children = mutableListOf<RoutingInit>()

    fun path(parts: RoutePath, init: RoutingInit.() -> Unit) {
        children += RoutingInit(this.pathParts + parts).apply(init)
    }

    fun get(
        produces: String? = null,
        handler: TestHttpHandler<GetRequest>
    ) {
        routes += { basePath: String, pathSpec: RoutePath ->
            GetRoute(pathSpec, TestHandlerWrapper(basePath, handler), produces)
        }
    }

    fun post(
        consumes: String? = null,
        produces: String? = null,
        handler: TestHttpHandler<PostRequest>
    ) {
        routes += { basePath: String, pathSpec: RoutePath ->
            PostRoute(pathSpec, TestHandlerWrapper(basePath, handler), consumes, produces)
        }
    }

    fun put(
        consumes: String? = null,
        produces: String? = null,
        handler: TestHttpHandler<PutRequest>
    ) {
        routes += { basePath: String, pathSpec: RoutePath ->
            PutRoute(pathSpec, TestHandlerWrapper(basePath, handler), consumes, produces)
        }
    }

    fun patch(
        consumes: String? = null,
        produces: String? = null,
        handler: TestHttpHandler<PatchRequest>
    ) {
        routes += { basePath: String, pathSpec: RoutePath ->
            PatchRoute(pathSpec, TestHandlerWrapper(basePath, handler), consumes, produces)
        }
    }

    fun delete(
        produces: String? = null,
        handler: TestHttpHandler<DeleteRequest>
    ) {
        routes += { basePath: String, pathSpec: RoutePath ->
            DeleteRoute(pathSpec, TestHandlerWrapper(basePath, handler), produces)
        }
    }

    internal fun buildRoutes(basePath: List<StaticPathPart>): List<HttpRouteDefinition<*>> {
        val basePathString = basePath.joinToString("/", prefix = "/") { it.part }
        return routes.map {
            it.invoke(basePathString, basePath + this.pathParts)
        } + children.flatMap {
            it.buildRoutes(basePath)
        }
    }
}

typealias TestHttpHandler<R> = suspend R.() -> TestResponse

// Normalize and shorten pathSpec names
internal fun String.toPathSegment(maxLength: Int = 30): String {
    return this.toLowerCase()
        .replace("'", "")
        .replace("""(to|the|a) """.toRegex(), "")
        .replace("""[^-_0-9a-z]+""".toRegex(), "_")
        .take(maxLength)
}
