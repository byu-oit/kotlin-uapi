package edu.byu.uapi.http

import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceFetchHandler
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceRequestHandler
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceRuntime
import edu.byu.uapi.server.types.ModelHolder

class HttpSingletonSubresource<UserContext : Any, Parent : ModelHolder, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val config: HttpEngineConfig,
    val subresource: SingletonSubresourceRuntime<UserContext, Parent, Model>,
    val rootPath: List<PathPart>
) {

    val routes: List<HttpRoute> by lazy {
        val path = rootPath + StaticPathPart(subresource.name)

        subresource.availableOperations.map { handlerFor(it, path) }
    }

    private fun handlerFor(
        op: SingletonSubresourceRequestHandler<UserContext, Parent, Model, *>,
        path: List<PathPart>
    ): HttpRoute {
        return when (op) {
            is SingletonSubresourceFetchHandler -> {
//                HttpRoute(
//                    path, HttpMethod.GET,
//                )
                TODO()
            }
        }
    }

}

class SingletonSubresourceFetchHttpHandler<UserContext: Any, Parent: ModelHolder, Model: Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: SingletonSubresourceFetchHandler<UserContext, Parent, Model>
): AuthenticatedHandler<UserContext>(runtime) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
//        val response = handler.handle(FetchSingletonSubresource(
//            request.asRequestContext(),
//            userContext,
//
//        ))
        TODO("not implemented")
    }

}

