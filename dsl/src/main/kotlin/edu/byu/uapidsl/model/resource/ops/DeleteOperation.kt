package edu.byu.uapidsl.model.resource.ops

import edu.byu.uapidsl.dsl.DeleteAuthorizer
import edu.byu.uapidsl.dsl.DeleteContext
import edu.byu.uapidsl.dsl.DeleteHandler
import edu.byu.uapidsl.model.resource.DeleteResourceRequest
import edu.byu.uapidsl.model.resource.DomainModelOps
import edu.byu.uapidsl.types.ErrorResponse
import edu.byu.uapidsl.types.UAPIEmptyResponse
import edu.byu.uapidsl.types.UAPIResponse

data class DeleteOperation<AuthContext, IdType, DomainType>(
    val authorization: DeleteAuthorizer<AuthContext, IdType, DomainType>,
    val handle: DeleteHandler<AuthContext, IdType, DomainType>
) {
    fun handleRequest(
        request: DeleteResourceRequest<AuthContext, IdType>,
        modelOps: DomainModelOps<AuthContext, IdType, DomainType>
    ): UAPIResponse<*> {
        val auth = request.authContext
        val id = request.id
        val model = modelOps.idToModel(auth, id)

        if (model != null) {
            val context = DeleteContextImpl(auth, id, model)

            val authorized = authorization.invoke(context)

            if (!authorized) {
                return ErrorResponse.notAuthorized()
            }

            handle.invoke(context)
        }

        return UAPIEmptyResponse
    }
}

internal data class DeleteContextImpl<AuthContext, IdType, ResourceModel>(
    override val authContext: AuthContext,
    override val id: IdType,
    override val resource: ResourceModel
) : DeleteContext<AuthContext, IdType, ResourceModel>
