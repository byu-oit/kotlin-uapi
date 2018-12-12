package edu.byu.uapi.http

import edu.byu.uapi.http.json.JsonEngine
import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.UserContextAuthnInfo
import edu.byu.uapi.server.UserContextFactory
import edu.byu.uapi.server.UserContextResult
import edu.byu.uapi.server.resources.list.*
import edu.byu.uapi.server.types.GenericUAPIErrorResponse
import edu.byu.uapi.server.types.UAPINotAuthenticatedError
import edu.byu.uapi.server.types.UAPIResponse
import edu.byu.uapi.spi.SpecConstants
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.IdParamMeta
import edu.byu.uapi.spi.rendering.Renderable
import edu.byu.uapi.spi.rendering.Renderer
import edu.byu.uapi.spi.requests.*

class HttpListResource<UserContext : Any, Id : Any, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val config: HttpEngineConfig,
    val resource: ListResourceRuntime<UserContext, Id, Model, *>
) {

   val httpSubresources = resource.subresources.values.map { it.toHttp(runtime, config) }

    val routes: List<HttpRoute> by lazy {
        val rootPath = listOf(StaticPathPart(resource.pluralName))
        val idPath = rootPath + resource.idReader.describe().toPathPart()

        val list = resource.availableOperations.map { handlerFor(it, rootPath, idPath) }

        val subresourceRoutes = httpSubresources.flatMap {
            it.getRoutes(idPath)
        }

        list + subresourceRoutes
    }

//    private fun subresourceHandlerFor(
//        subresource: SubresourceRuntime<UserContext, IdentifiedModel<Id, Model>, Model>,
//        idPath: List<PathPart>
//    ): List<HttpRoute> {
//        return when(subresource) {
//            is SingletonSubresourceRuntime -> {
//                val path = idPath + StaticPathPart(subresource.name)
//                subresource.availableOperations.map { op ->
//                    HttpRoute(
//                        path,
//                    )
//                }
//            }
//        }
//    }

    private fun handlerFor(
        op: ListResourceRequestHandler<UserContext, Id, Model, *, *>,
        rootPath: List<PathPart>,
        idPath: List<PathPart>
    ): HttpRoute {
        return when (op) {
            is ListResourceFetchHandler -> HttpRoute(
                idPath, HttpMethod.GET, IdentifiedResourceFetchHttpHandler(runtime, op)
            )
            is ListResourceListHandler<UserContext, Id, Model, *> -> HttpRoute(
                rootPath, HttpMethod.GET, IdentifiedResourceListHttpHandler(runtime, op)
            )
            is ListResourceCreateHandler<UserContext, Id, Model, *, *> -> HttpRoute(
                rootPath, HttpMethod.POST, IdentifiedResourceCreateHttpHandler(runtime, op, config.jsonEngine)
            )
            is ListResourceUpdateHandler<UserContext, Id, Model, *, *> -> HttpRoute(
                idPath, HttpMethod.PUT, IdentifiedResourceUpdateHttpHandler(runtime, op, config.jsonEngine)
            )
            is ListResourceDeleteHandler<UserContext, Id, Model, *> -> HttpRoute(
                idPath, HttpMethod.DELETE, IdentifiedResourceDeleteHttpHandler(runtime, op)
            )
        }
    }
}

fun IdParamMeta.toPathPart(): PathPart {
    val params = this.idParams
    return if (params.size == 1) {
        SimplePathVariablePart(params.first().name)
    } else {
        CompoundPathVariablePart(params.map { it.name })
    }
}

fun <E> Collection<E>.containsAny(vararg element: @UnsafeVariance E): Boolean {
    return element.any { it in this }
}

abstract class AuthenticatedHandler<UserContext : Any>(
    private val factory: UserContextFactory<UserContext>,
    private val typeDictionary: TypeDictionary
) : HttpHandler {
    constructor(runtime: UAPIRuntime<UserContext>) : this(runtime.userContextFactory, runtime.typeDictionary)

    override fun handle(request: HttpRequest): HttpResponse {
        val authResult = factory.createUserContext(HttpUserContextAuthnInfo(request))
        return when (authResult) {
            is UserContextResult.Success -> handleAuthenticated(request, authResult.result)
            is UserContextResult.Failure -> UAPINotAuthenticatedError(authResult.messages).toHttpResponse()
        }
    }

    abstract fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse
}

fun UAPIResponse<*>.toHttpResponse(): HttpResponse {
    return UAPIHttpResponse(this)
}

class UAPIHttpResponse(
    response: UAPIResponse<*>
) : HttpResponse {
    override val status: Int = response.metadata.validationResponse.code
    override val headers: Map<String, Set<String>> = emptyMap()
    override val body: ResponseBody = if (this.status == 404 || this.status == 204) EmptyResponseBody else RenderableResponseBody(response)
}

class RenderableResponseBody(
    private val wrapped: Renderable
) : ResponseBody {
    override fun <Output: Any> render(renderer: Renderer<Output>): Output {
        wrapped.render(renderer)
        return renderer.finalize()
    }
}

class EmptyResponse(
    override val status: Int,
    override val headers: Map<String, Set<String>> = emptyMap()
) : HttpResponse {
    override val body: ResponseBody = EmptyResponseBody
}

class HttpUserContextAuthnInfo(
    req: HttpRequest
) : UserContextAuthnInfo {
    override val headers: Headers = req.headers
    override val queryParams: Map<String, Set<String>> = req.query
    override val requestUrl: String
        get() = TODO("not implemented")
    override val relativePath: String
        get() = TODO("not implemented")
    override val remoteIp: String
        get() = TODO("not implemented")

}

class IdentifiedResourceFetchHttpHandler<UserContext : Any, Id : Any, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: ListResourceFetchHandler<UserContext, Id, Model, *>
) : AuthenticatedHandler<UserContext>(runtime) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
        val response = handler.handle(FetchListResource(
            request.asRequestContext(),
            userContext,
            request.path.asIdParams(),
            request.query.asQueryParams()
        ))
        return response.toHttpResponse()
    }
}

class IdentifiedResourceListHttpHandler<UserContext : Any, Id : Any, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: ListResourceListHandler<UserContext, Id, Model, *>
) : AuthenticatedHandler<UserContext>(runtime) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
        val response = handler.handle(ListListResource(
            request.asRequestContext(),
            userContext,
            request.query.asQueryParams()
        ))
        return response.toHttpResponse()
    }
}

class IdentifiedResourceCreateHttpHandler<UserContext: Any, Id: Any, Model: Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: ListResourceCreateHandler<UserContext, Id, Model, *, *>,
    val jsonEngine: JsonEngine<*, *>
): AuthenticatedHandler<UserContext>(runtime) {
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
        val response = handler.handle(CreateListResource(
            request.asRequestContext(),
            userContext,
            wrappedBody
        ))
        return response.toHttpResponse()
    }
}

class IdentifiedResourceUpdateHttpHandler<UserContext: Any, Id: Any, Model: Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: ListResourceUpdateHandler<UserContext, Id, Model, *, *>,
    val jsonEngine: JsonEngine<*, *>
): AuthenticatedHandler<UserContext>(runtime) {
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
        val response = handler.handle(UpdateListResource(
            request.asRequestContext(),
            userContext,
            request.path.asIdParams(),
            wrappedBody
        ))
        return response.toHttpResponse()
    }
}

class IdentifiedResourceDeleteHttpHandler<UserContext: Any, Id: Any, Model: Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: ListResourceDeleteHandler<UserContext, Id, Model, *>
): AuthenticatedHandler<UserContext>(runtime) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
        val response = handler.handle(DeleteListResource(
            request.asRequestContext(),
            userContext,
            request.path.asIdParams()
        ))
        return response.toHttpResponse()
    }
}

fun HttpPathParams.asIdParams(): IdParams {
    return this.mapValues { StringIdParam(it.key, it.value) }
}

fun HttpQueryParams.asQueryParams(): QueryParams {
    return this.mapValues { StringSetQueryParam(it.key, it.value) }
}

fun HttpRequest.asRequestContext() = HttpRequestContext(this)

data class HttpRequestContext(val request: HttpRequest) : RequestContext {
    override val fieldsets: FieldsetRequest? = request.getFieldsetRequest()
    override val baseUri: String = ""
    override val headers: Headers = request.headers
}

internal fun HttpRequest.getFieldsetRequest(): FieldsetRequest? {
    val query = this.query
    val fieldsets = query[SpecConstants.FieldSets.Query.KEY_FIELD_SETS].orEmpty()
        .flatMap { it.split(",").filter(String::isNotBlank) }
    val contexts = query[SpecConstants.FieldSets.Query.KEY_CONTEXTS].orEmpty()
        .flatMap { it.split(",").filter(String::isNotBlank) }

    if (fieldsets.isEmpty() && contexts.isEmpty()) {
        return null
    }
    return FieldsetRequest(fieldsets.toSet(), contexts.toSet())
}
