package edu.byu.uapi.http

import edu.byu.uapi.http.json.JavaxJsonTreeRenderer
import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.UserContextAuthnInfo
import edu.byu.uapi.server.UserContextFactory
import edu.byu.uapi.server.UserContextResult
import edu.byu.uapi.server.resources.identified.IdentifiedResourceFetchHandler
import edu.byu.uapi.server.resources.identified.IdentifiedResourceListHandler
import edu.byu.uapi.server.resources.identified.IdentifiedResourceRequestHandler
import edu.byu.uapi.server.resources.identified.IdentifiedResourceRuntime
import edu.byu.uapi.server.types.UAPINotAuthenticatedError
import edu.byu.uapi.server.types.UAPIResponse
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.IdParamMeta
import edu.byu.uapi.spi.rendering.Renderable
import edu.byu.uapi.spi.requests.*
import java.io.StringWriter
import java.io.Writer
import javax.json.spi.JsonProvider

class HttpIdentifiedResource<UserContext : Any, Id : Any, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val resource: IdentifiedResourceRuntime<UserContext, Id, Model>
) {
    val routes: List<HttpRoute> by lazy {
        val rootPath = listOf(StaticPathPart(resource.name))
        val idPath = rootPath + resource.idReader.describe().toPathPart()

        resource.availableOperations.map { handlerFor(it, rootPath, idPath) }
    }

    private fun handlerFor(
        op: IdentifiedResourceRequestHandler<UserContext, Id, Model, *>,
        rootPath: List<PathPart>,
        idPath: List<PathPart>
    ): HttpRoute {
//        return when (op) {
//            FETCH -> HttpRoute(
//                idPath, HttpMethod.GET, IdentifiedResourceFetchHandler(
//                runtime, resource
//            ))
//            CREATE -> TODO()
//            UPDATE -> TODO()
//            DELETE -> TODO()
//            LIST -> TODO()
//        }
        return when (op) {
            is IdentifiedResourceFetchHandler -> HttpRoute(
                idPath, HttpMethod.GET, IdentifiedResourceFetchHttpHandler(runtime, op)
            )
            is IdentifiedResourceListHandler<UserContext, Id, Model, *> -> HttpRoute(
                rootPath, HttpMethod.GET, IdentifiedResourceListHttpHandler(runtime, op)
            )
        }
    }
}

private fun IdParamMeta.toPathPart(): PathPart {
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
            is UserContextResult.Failure -> UAPINotAuthenticatedError(authResult.messages).toHttpResponse(typeDictionary)
        }
    }

    abstract fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse
}

fun UAPIResponse<*>.toHttpResponse(typeDictionary: TypeDictionary): HttpResponse {
    return UAPIHttpResponse(this, typeDictionary)
}

class UAPIHttpResponse(
    response: UAPIResponse<*>,
    typeDictionary: TypeDictionary
) : HttpResponse {
    override val status: Int = response.metadata.validationResponse.code
    override val headers: HttpHeaders = emptyMap()
    override val body: ResponseBody = if (this.status == 404) EmptyResponseBody else RenderableResponseBody(response, typeDictionary)
}

val jsonProvider: JsonProvider = JsonProvider.provider()

class RenderableResponseBody(
    private val wrapped: Renderable,
    private val typeDictionary: TypeDictionary
) : ResponseBody {
    override fun asString(): String {
        return StringWriter().use {
            toWriter(it)
            it.toString()
        }
    }

    override fun toWriter(writer: Writer) {
        val json = JavaxJsonTreeRenderer(typeDictionary, jsonProvider)
        wrapped.render(json)
        val obj = json.render()
        jsonProvider.createWriter(writer).use {
            it.write(obj)
        }
    }
}

class EmptyResponse(
    override val status: Int,
    override val headers: HttpHeaders = emptyMap()
) : HttpResponse {
    override val body: ResponseBody = EmptyResponseBody
}

class HttpUserContextAuthnInfo(
    req: HttpRequest
) : UserContextAuthnInfo {
    override val headers: Map<String, Set<String>> = req.headers
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
    val handler: IdentifiedResourceFetchHandler<UserContext, Id, Model>
) : AuthenticatedHandler<UserContext>(runtime) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
        val response = handler.handle(FetchIdentifiedResource(
            request.asRequestContext(),
            userContext,
            request.path.asIdParams(),
            request.query.asQueryParams()
        ))
        return response.toHttpResponse(runtime.typeDictionary)
    }
}

class IdentifiedResourceListHttpHandler<UserContext : Any, Id : Any, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: IdentifiedResourceListHandler<UserContext, Id, Model, *>
) : AuthenticatedHandler<UserContext>(runtime) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
        val response = handler.handle(ListIdentifiedResource(
            request.asRequestContext(),
            userContext,
            request.query.asQueryParams()
        ))
        return response.toHttpResponse(runtime.typeDictionary)
    }
}

private fun HttpPathParams.asIdParams(): IdParams {
    return this.mapValues { StringIdParam(it.key, it.value) }
}

private fun HttpQueryParams.asQueryParams(): QueryParams {
    return this.mapValues { StringSetQueryParam(it.key, it.value) }
}

fun HttpRequest.asRequestContext() = HttpRequestContext(this)

data class HttpRequestContext(val request: HttpRequest) : RequestContext {
    override val baseUri: String = ""
    override val headers: Map<String, Set<String>> = request.headers
}
