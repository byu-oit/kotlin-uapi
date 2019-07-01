package edu.byu.uapi.http

import edu.byu.uapi.http.json.JsonEngine
import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceDeleteHandler
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceFetchHandler
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceRequest
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceUpdateHandler
import edu.byu.uapi.server.types.GenericUAPIErrorResponse
import edu.byu.uapi.server.types.ModelHolder

class SingletonSubresourceFetchHttpHandler<UserContext : Any, Parent : ModelHolder, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: SingletonSubresourceFetchHandler<UserContext, Parent, Model>
) : AuthenticatedHandler<UserContext>(runtime) {
    override fun handleAuthenticated(
        request: HttpRequest,
        userContext: UserContext
    ): HttpResponse {
        val response = handler.handle(SingletonSubresourceRequest.Fetch(
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
        val response = handler.handle(SingletonSubresourceRequest.Delete(
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
        val response = handler.handle(SingletonSubresourceRequest.Update(
            request.asRequestContext(),
            userContext,
            request.path.asIdParams(),
            wrappedBody
        ))
        return response.toHttpResponse()
    }
}
