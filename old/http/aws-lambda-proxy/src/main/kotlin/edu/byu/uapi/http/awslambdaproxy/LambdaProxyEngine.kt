package edu.byu.uapi.http.awslambdaproxy

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import edu.byu.uapi.http.*
import edu.byu.uapi.http.docs.DocSource
import edu.byu.uapi.http.json.*
import edu.byu.uapi.server.UAPIRuntime
import java.io.StringWriter

data class LambdaConfig(override val jsonEngine: JsonEngine<*, *>) : HttpEngineConfig

interface LambdaRequestHandler {
    val pattern: Regex
    val method: HttpMethod
    fun handle(request: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent
}

data class APILambdaRequestHandler(
    override val pattern: Regex,
    val paramsNames: List<String>,
    override val method: HttpMethod,
    val handler: HttpHandler,
    val runtime: UAPIRuntime<*>,
    val jsonEngine: JsonEngine<*, *>
) : LambdaRequestHandler {
    override fun handle(request: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent {
        val response = this.handler.handle(LambdaRequest(request, pattern, paramsNames))

        val rendered = when (jsonEngine) {
            is GsonTreeEngine -> {
                val result = response.body.render(jsonEngine.renderer(runtime.typeDictionary, null))
                result.toString()
            }
            is JavaxJsonTreeEngine -> {
                val obj = response.body.render(jsonEngine.renderer(runtime.typeDictionary, null))
                obj.toString()
            }
            is JavaxJsonStreamEngine -> {
                val writer = StringWriter()
                response.body.render(jsonEngine.renderer(runtime.typeDictionary, writer))
                writer.toString()
            }
            is JacksonEngine -> {
                val writer = StringWriter()
                response.body.render(jsonEngine.renderer(runtime.typeDictionary, writer))
                writer.toString()
            }
        }
        return APIGatewayProxyResponseEvent()
            .withStatusCode(response.status)
            .withHeaders(response.headers.mapValues { it.value.first() })
            .withBody(rendered)
    }
}

class LambdaProxyEngine(config: LambdaConfig) : HttpEngineBase<LambdaServer, LambdaConfig>(config) {
    init {
        super.doInit()
    }

    override fun startServer(config: LambdaConfig): LambdaServer {
        return LambdaServer()
    }

    override fun stop(server: LambdaServer) {
    }

    private val mappedPaths = mutableListOf<LambdaRequestHandler>()

    override fun registerRoutes(
        server: LambdaServer,
        config: LambdaConfig,
        routes: List<HttpRoute>,
        rootPath: String,
        runtime: UAPIRuntime<*>
    ) {
        val root = if (rootPath.isNotBlank() && !rootPath.startsWith("/")) "/$rootPath" else rootPath
        mappedPaths += routes.map { it.toHandler(root, runtime, config) }
    }

    override fun registerDocRoutes(
        server: LambdaServer,
        config: LambdaConfig,
        docRoutes: List<DocRoute>,
        rootPath: String,
        runtime: UAPIRuntime<*>
    ) {
        val root = if (rootPath.isNotBlank() && !rootPath.startsWith("/")) "/$rootPath" else rootPath
        mappedPaths += docRoutes.map { it.toHandler(root) }
    }

    fun dispatch(
        input: APIGatewayProxyRequestEvent,
        context: Context
    ): APIGatewayProxyResponseEvent {
        val method = HttpMethod.valueOf(input.httpMethod.toUpperCase())
        val path: String = input.path
        val found = mappedPaths.find { it.method == method && it.pattern.matches(path) }

        return found?.handle(input) ?: NOT_FOUND_RESPONSE
    }
}

val NOT_FOUND_RESPONSE: APIGatewayProxyResponseEvent = APIGatewayProxyResponseEvent()
    .withStatusCode(404)
    .withBody("")

fun HttpRoute.toHandler(
    rootPath: String,
    runtime: UAPIRuntime<*>,
    config: LambdaConfig
): LambdaRequestHandler {
    val (regex, groups) = this.pathParts.toRoutePattern(rootPath)
    return APILambdaRequestHandler(regex, groups, this.method, this.handler, runtime, config.jsonEngine)
}

fun DocRoute.toHandler(
    rootPath: String
): LambdaRequestHandler {
    val (regex, _) = this.path.toRoutePattern(rootPath)
    return DocLambdaRequestHandler(regex, this.source)
}

class DocLambdaRequestHandler(override val pattern: Regex, private val source: DocSource) :
    LambdaRequestHandler {
    override val method: HttpMethod = HttpMethod.GET

    override fun handle(request: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent {
        val pretty = request.queryStringParameters.orEmpty()["pretty"]?.toBoolean() ?: false

        val body = source.getInputStream(pretty).use { it.reader().use { r -> r.readText() } }

        return APIGatewayProxyResponseEvent()
            .withStatusCode(200)
            .withHeaders(mapOf("Content-Type" to source.contentType))
            .withBody(body)
    }

}

fun List<PathPart>.toRoutePattern(rootPath: String): HttpRoutePattern {
    val groups = mutableListOf<String>()
    val parts = this.stringify {
        groups.add(it)
        "(.+?)"
    }
    return HttpRoutePattern("^$rootPath$parts\$".toRegex(), groups)
}

data class HttpRoutePattern(
    val pattern: Regex,
    val groups: List<String>
)

class LambdaServer

