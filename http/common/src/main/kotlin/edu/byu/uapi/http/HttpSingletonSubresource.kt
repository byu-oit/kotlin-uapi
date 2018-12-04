package edu.byu.uapi.http

import edu.byu.uapi.http.json.JsonEngine
import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.subresources.singleton.*
import edu.byu.uapi.server.types.GenericUAPIErrorResponse
import edu.byu.uapi.server.types.ModelHolder

sealed class HttpSubresource {
    abstract fun getRoutes(rootPath: List<PathPart>): List<HttpRoute>
}

fun <UserContext : Any, Parent : ModelHolder, Model : Any> SubresourceRuntime<UserContext, Parent, Model>.toHttp(
    runtime: UAPIRuntime<UserContext>,
    config: HttpEngineConfig
): HttpSubresource {
    return when (this) {
        is SingletonSubresourceRuntime -> HttpSingletonSubresource(runtime, config, this)
    }
}

class HttpSingletonSubresource<UserContext : Any, Parent : ModelHolder, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val config: HttpEngineConfig,
    val subresource: SingletonSubresourceRuntime<UserContext, Parent, Model>
) : HttpSubresource() {

    override fun getRoutes(rootPath: List<PathPart>): List<HttpRoute> {
        val path = rootPath + StaticPathPart(subresource.name)

        return subresource.availableOperations.map { handlerFor(it, path) }
    }

    private fun handlerFor(
        op: SingletonSubresourceRequestHandler<UserContext, Parent, Model, *>,
        path: List<PathPart>
    ): HttpRoute {
        return when (op) {
            is SingletonSubresourceFetchHandler ->
                HttpRoute(path, HttpMethod.GET, SingletonSubresourceFetchHttpHandler(runtime, op))
            is SingletonSubresourceDeleteHandler ->
                HttpRoute(path, HttpMethod.DELETE, SingletonSubresourceDeleteHttpHandler(runtime, op))
            is SingletonSubresourceUpdateHandler<UserContext, Parent, Model, *> ->
                HttpRoute(path, HttpMethod.PUT, SingletonSubresourceUpdateHttpHandler(runtime, op, config.jsonEngine))
        }
    }
}

class SingletonSubresourceFetchHttpHandler<UserContext : Any, Parent : ModelHolder, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: SingletonSubresourceFetchHandler<UserContext, Parent, Model>
) : AuthenticatedHandler<UserContext>(runtime) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
        val response = handler.handle(FetchSingletonSubresource(
            request.asRequestContext(),
            userContext,
            request.path.asIdParams()
        ))
        return response.toHttpResponse()
    }
}

class SingletonSubresourceDeleteHttpHandler<UserContext : Any, Parent : ModelHolder, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: SingletonSubresourceDeleteHandler<UserContext, Parent, Model>
) : AuthenticatedHandler<UserContext>(runtime) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
        val response = handler.handle(DeleteSingletonSubresource(
            request.asRequestContext(),
            userContext,
            request.path.asIdParams()
        ))
        return response.toHttpResponse()
    }
}

class SingletonSubresourceUpdateHttpHandler<UserContext : Any, Parent : ModelHolder, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: SingletonSubresourceUpdateHandler<UserContext, Parent, Model, *>,
    val jsonEngine: JsonEngine<*, *>
) : AuthenticatedHandler<UserContext>(runtime) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
        val body = request.body ?: return UAPIHttpResponse(GenericUAPIErrorResponse(
            statusCode = 400,
            message = "Missing Request Body",
            validationInformation = listOf("Expected a request body. Please try your request again.")
        ))

        val wrappedBody = jsonEngine.resourceBody(body, runtime.typeDictionary)
        val response = handler.handle(UpdateSingletonSubresource(
            request.asRequestContext(),
            userContext,
            request.path.asIdParams(),
            wrappedBody
        ))
        return response.toHttpResponse()
    }
}

