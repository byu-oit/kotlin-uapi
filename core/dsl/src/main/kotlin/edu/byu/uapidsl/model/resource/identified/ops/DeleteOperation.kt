package edu.byu.uapidsl.model.resource.identified.ops

import edu.byu.uapidsl.model.resource.Authorizer
import edu.byu.uapidsl.model.resource.Handler
import edu.byu.uapidsl.model.resource.ResourceDeleteContext
import edu.byu.uapidsl.model.resource.ResourceDeleteOperation
import edu.byu.uapidsl.model.resource.identified.*
import edu.byu.uapidsl.types.ErrorResponse
import edu.byu.uapidsl.types.UAPIEmptyResponse
import edu.byu.uapidsl.types.UAPIResponse

internal typealias DeleteContext<Auth, Id, Model> =
    ResourceDeleteContext<Auth, Model, IdentifiedResourceModelContext<Id, Model>>

class DeleteOperation<Auth: Any, Id: Any, Model: Any>(
    override val authorized: Authorizer<DeleteContext<Auth, Id, Model>>,
    override val handle: Handler<DeleteContext<Auth, Id, Model>>
): ResourceDeleteOperation<Auth, Model, IdentifiedResourceModelContext<Id, Model>> {
    fun handleRequest(
        request: DeleteResourceRequest<Auth, Id>,
        modelOps: DomainModelOps<Auth, Id, Model>
    ): UAPIResponse<*> {
        val auth = request.authContext
        val id = request.id
        val model = modelOps.idToModel(auth, id)

        if (model != null) {
            val context = ResourceDeleteContext.Default(auth, IdentifiedResourceModelContext(id, model))

            val authorized = authorized.invoke(context)

            if (!authorized) {
                return ErrorResponse.notAuthorized()
            }

            handle.invoke(context)
        }

        return UAPIEmptyResponse
    }
}

