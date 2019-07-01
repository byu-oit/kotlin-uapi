package edu.byu.uapi.http

import edu.byu.uapi.http.json.JsonEngine
import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.subresources.list.*
import edu.byu.uapi.server.types.GenericUAPIErrorResponse
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.spi.requests.ListSubresourceRequest

sealed class ListSubresourceHttpHandler<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any,
    Request : ListSubresourceRequest<UserContext>>(
    val runtime: UAPIRuntime<UserContext>,
    val handler: ListSubresourceRequestHandler<UserContext, Parent, Id, Model, *, Request>
) : AuthenticatedHandler<UserContext>(runtime) {

    class Fetch<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any>(
        runtime: UAPIRuntime<UserContext>,
        handler: ListSubresourceFetchHandler<UserContext, Parent, Id, Model, *>
    ) : ListSubresourceHttpHandler<UserContext, Parent, Id, Model, ListSubresourceRequest.Fetch<UserContext>>(runtime, handler) {
        override fun handleAuthenticated(
            request: HttpRequest,
            userContext: UserContext
        ): HttpResponse {
            val id = request.path.asIdParams()
            return handler.handle(ListSubresourceRequest.Fetch(
                request.asRequestContext(),
                userContext,
                id,
                id,
                request.query.asQueryParams()
            )).toHttpResponse()
        }
    }

    class List<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any>(
        runtime: UAPIRuntime<UserContext>,
        handler: ListSubresourceListHandler<UserContext, Parent, Id, Model, *>
    ) : ListSubresourceHttpHandler<UserContext, Parent, Id, Model, ListSubresourceRequest.List<UserContext>>(runtime, handler) {
        override fun handleAuthenticated(
            request: HttpRequest,
            userContext: UserContext
        ): HttpResponse {
            val id = request.path.asIdParams()
            return handler.handle(ListSubresourceRequest.List(
                request.asRequestContext(),
                userContext,
                id,
                request.query.asQueryParams()
            )).toHttpResponse()
        }
    }

    class Create<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any>(
        runtime: UAPIRuntime<UserContext>,
        handler: ListSubresourceCreateHandler<UserContext, Parent, Id, Model, *, *>,
        private val jsonEngine: JsonEngine<*, *>
    ) : ListSubresourceHttpHandler<UserContext, Parent, Id, Model, ListSubresourceRequest.Create<UserContext>>(runtime, handler) {
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
            val id = request.path.asIdParams()
            return handler.handle(ListSubresourceRequest.Create(
                request.asRequestContext(),
                userContext,
                id,
                wrappedBody
            )).toHttpResponse()
        }
    }

    class Update<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any>(
        runtime: UAPIRuntime<UserContext>,
        handler: ListSubresourceUpdateHandler<UserContext, Parent, Id, Model, *, *>,
        private val jsonEngine: JsonEngine<*, *>
    ) : ListSubresourceHttpHandler<UserContext, Parent, Id, Model, ListSubresourceRequest.Update<UserContext>>(runtime, handler) {
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
            val id = request.path.asIdParams()
            return handler.handle(ListSubresourceRequest.Update(
                request.asRequestContext(),
                userContext,
                id,
                id,
                wrappedBody
            )).toHttpResponse()
        }
    }

    class Delete<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any>(
        runtime: UAPIRuntime<UserContext>,
        handler: ListSubresourceDeleteHandler<UserContext, Parent, Id, Model, *>
    ) : ListSubresourceHttpHandler<UserContext, Parent, Id, Model, ListSubresourceRequest.Delete<UserContext>>(runtime, handler) {
        override fun handleAuthenticated(
            request: HttpRequest,
            userContext: UserContext
        ): HttpResponse {
            val id = request.path.asIdParams()
            return handler.handle(ListSubresourceRequest.Delete(
                request.asRequestContext(),
                userContext,
                id,
                id
            )).toHttpResponse()
        }
    }
}


