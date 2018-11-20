package edu.byu.uapi.http.awslambdaproxy

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import edu.byu.uapi.http.*
import edu.byu.uapi.http.json.*
import edu.byu.uapi.server.UAPIRuntime
import java.io.StringWriter

data class LambdaConfig(override val jsonEngine: JsonEngine<*, *>) : HttpEngineConfig

data class LambdaRequestHandler(
    val pattern: Regex,
    val paramsNames: List<String>,
    val method: HttpMethod,
    val handler: HttpHandler,
    val runtime: UAPIRuntime<*>,
    val jsonEngine: JsonEngine<*, *>
) {
    fun handle(request: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent {
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
    val (regex, groups) = this.toRegex(rootPath)
    return LambdaRequestHandler(regex, groups, this.method, this.handler, runtime, config.jsonEngine)
}

fun HttpRoute.toRegex(rootPath: String): HttpRoutePattern {
    val groups = mutableListOf<String>()
    val parts = this.pathParts.stringify {
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

