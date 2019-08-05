package edu.byu.uapi.server.http._internal

import edu.byu.uapi.server.http.engines.HttpEngine
import edu.byu.uapi.server.http.engines.HttpResponse
import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.engines.RequestReader
import edu.byu.uapi.server.http.engines.RouteMethod
import edu.byu.uapi.server.http.path.PathFormatter
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.format
import edu.byu.uapi.server.http.path.extractVariableValues
import java.io.InputStream

sealed class HttpRouteDefinition<R : HttpRequest>(
    val routeMethod: RouteMethod
) {
    // The pathSpec of the route, relative to the root of the UAPI instance pathspace
    abstract val path: RoutePath
    abstract val handler: HttpHandler<R>
    abstract val produces: String?

    fun <E : Any> buildRoute(engine: HttpEngine<E>): HttpRoute<E> {
        return HttpRouteImpl(this, engine)
    }

    internal suspend fun <E> buildRequest(
        engineRequest: E,
        reader: RequestReader<E>,
        pathFormatter: PathFormatter
    ): R {
        return reader.run {
            val reqPath = path(engineRequest)
            val headers = headerNames(engineRequest)
                .associate { it.toLowerCase() to headerValue(engineRequest, it) }
            val queryParams = queryParameters(engineRequest)
            val rawPathParams = pathParameters(engineRequest)

            val pathParams = pathFormatter.extractVariableValues(path, rawPathParams)

            createRequest(engineRequest, reader, reqPath, headers, queryParams, pathParams)
        }
    }

    @Suppress("LongParameterList")
    abstract suspend fun <E> createRequest(
        engineRequest: E,
        reader: RequestReader<E>,
        path: String,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        pathParams: Map<String, String>
    ): R
}

sealed class HttpRouteWithBody<R : HttpRequestWithBody>(
    method: RouteMethod
) : HttpRouteDefinition<R>(method) {
    abstract val consumes: String?

    override suspend fun <E> createRequest(
        engineRequest: E,
        reader: RequestReader<E>,
        path: String,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        pathParams: Map<String, String>
    ): R {
        val body = reader.run {
            bodyStream(engineRequest)
        }
        return createRequest(
            path, headers, queryParams, pathParams, body
        )
    }

    abstract fun createRequest(
        path: String,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        pathParams: Map<String, String>,
        body: InputStream
    ): R
}

sealed class HttpRouteNoBody<R : HttpRequest>(
    method: RouteMethod
) : HttpRouteDefinition<R>(method) {
    override suspend fun <E> createRequest(
        engineRequest: E,
        reader: RequestReader<E>,
        path: String,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        pathParams: Map<String, String>
    ): R {
        return createRequest(
            path, headers, queryParams, pathParams
        )
    }

    abstract fun createRequest(
        path: String,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        pathParams: Map<String, String>
    ): R
}

data class GetRoute(
    override val path: RoutePath,
    override val handler: GetHandler,
    override val produces: String? = null
) : HttpRouteNoBody<GetRequest>(RouteMethod.GET) {
    override fun createRequest(
        path: String,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        pathParams: Map<String, String>
    ) = GetRequest(path, headers, queryParams, pathParams)
}

data class DeleteRoute(
    override val path: RoutePath,
    override val handler: DeleteHandler,
    override val produces: String? = null
) : HttpRouteNoBody<DeleteRequest>(RouteMethod.DELETE) {
    override fun createRequest(
        path: String,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        pathParams: Map<String, String>
    ) = DeleteRequest(path, headers, queryParams, pathParams)
}

data class PutRoute(
    override val path: RoutePath,
    override val handler: PutHandler,
    override val consumes: String? = null,
    override val produces: String? = null
) : HttpRouteWithBody<PutRequest>(RouteMethod.PUT) {
    override fun createRequest(
        path: String,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        pathParams: Map<String, String>,
        body: InputStream
    ) = PutRequest(path, headers, queryParams, pathParams, body)
}

data class PostRoute(
    override val path: RoutePath,
    override val handler: PostHandler,
    override val consumes: String? = null,
    override val produces: String? = null
) : HttpRouteWithBody<PostRequest>(RouteMethod.POST) {
    override fun createRequest(
        path: String,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        pathParams: Map<String, String>,
        body: InputStream
    ) = PostRequest(path, headers, queryParams, pathParams, body)
}

data class PatchRoute(
    override val path: RoutePath,
    override val handler: PatchHandler,
    override val consumes: String? = null,
    override val produces: String? = null
) : HttpRouteWithBody<PatchRequest>(RouteMethod.PATCH) {
    override fun createRequest(
        path: String,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        pathParams: Map<String, String>,
        body: InputStream
    ) = PatchRequest(path, headers, queryParams, pathParams, body)
}

internal class HttpRouteImpl<E : Any, R : HttpRequest>(
    internal val definition: HttpRouteDefinition<R>,
    internal val engine: HttpEngine<E>
) : HttpRoute<E> {
    override val method = definition.routeMethod
    override val pathSpec = engine.pathFormatter.format(definition.path)
    override val consumes = (definition as? HttpRouteWithBody<*>)?.consumes
    override val produces = definition.produces

    override suspend fun dispatch(request: E): HttpResponse {
        val req = definition.buildRequest(request, engine.requestReader, engine.pathFormatter)
        return definition.handler.handle(req)
    }
}
