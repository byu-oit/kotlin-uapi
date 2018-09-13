package edu.byu.uapi.http

import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.UserContextAuthnInfo
import edu.byu.uapi.server.UserContextFactory
import edu.byu.uapi.server.UserContextResult
import edu.byu.uapi.server.resources.identified.IdentifiedResourceOperation
import edu.byu.uapi.server.resources.identified.IdentifiedResourceOperation.*
import edu.byu.uapi.server.resources.identified.IdentifiedResourceRuntime
import edu.byu.uapi.server.types.UAPINotAuthenticatedError
import edu.byu.uapi.server.types.UAPINotAuthorizedError
import edu.byu.uapi.server.types.UAPIResponse

class HttpIdentifiedResource<UserContext : Any, Id : Any, Model : Any>(
    val resource: IdentifiedResourceRuntime<UserContext, Id, Model>
) {
    val routes: List<HttpRoute> by lazy {
        val rootPath = listOf(StaticPathPart(resource.name))
        val idPath = rootPath + SimplePathVariablePart("id")

        resource.availableOperations.map { handlerFor(it, rootPath, idPath) }
    }

    private fun handlerFor(
        op: IdentifiedResourceOperation,
        rootPath: List<PathPart>,
        idPath: List<PathPart>
    ): HttpRoute {
        return when (op) {
//            FETCH -> HttpRoute(
//                idPath, HttpMethod.GET, IdentifiedResourceFetchHandler<UserContext, Id, Model>(
//                this
//            )
//            )
            FETCH -> TODO()
            CREATE -> TODO()
            UPDATE -> TODO()
            DELETE -> TODO()
            LIST -> TODO()
        }
    }
}

fun <E> Collection<E>.containsAny(vararg element: @UnsafeVariance E): Boolean {
    return element.any { it in this }
}

abstract class AuthenticatedHandler<UserContext : Any>(
    private val factory: UserContextFactory<UserContext>
) : HttpHandler {
    override fun handle(request: HttpRequest): HttpResponse {
        val authResult = factory.createUserContext(HttpUserContextAuthnInfo(request))
        return when(authResult) {
            is UserContextResult.Success -> handleAuthenticated(request, authResult.result)
            is UserContextResult.Failure -> UAPINotAuthenticatedError(authResult.messages).toHttpResponse()
        }
    }

    abstract fun handleAuthenticated(request: HttpRequest, userContext: UserContext): HttpResponse
}

fun UAPIResponse<*>.toHttpResponse(): HttpResponse {
    return UAPIHttpResponse(this)
}

class UAPIHttpResponse(
    val response: UAPIResponse<*>
): HttpResponse {
    override val status: Int = response.metadata.validationResponse.code
    override val headers: Headers = emptyMap()
    override val body: ResponseBody
        get() = TODO("not implemented")
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

class IdentifiedResourceFetchHandler<UserContext : Any, Id : Any, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val resource: IdentifiedResourceRuntime<UserContext, Id, Model>
)
    : AuthenticatedHandler<UserContext>(runtime.userContextFactory) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
        val resp = resource.handleFetch(userContext, resource.constructId(request.path, runtime.deserializationContext))
        TODO()
    }
}
