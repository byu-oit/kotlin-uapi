package edu.byu.uapidsl.model.resource.ops

import edu.byu.uapidsl.model.resource.CreateAuthorizer
import edu.byu.uapidsl.model.resource.CreateContext
import edu.byu.uapidsl.model.resource.CreateHandler
import edu.byu.uapidsl.model.resource.CreateResourceRequest
import edu.byu.uapidsl.model.resource.DomainModelOps
import edu.byu.uapidsl.model.resource.InputModel
import edu.byu.uapidsl.types.ErrorResponse
import edu.byu.uapidsl.types.UAPIResponse

data class CreateOperation<AuthContext, IdType, InputType : Any>(
    val input: InputModel<InputType>,
    val authorization: CreateAuthorizer<AuthContext, InputType>,
    val handle: CreateHandler<AuthContext, IdType, InputType>
) {
    fun handleRequest(
        request: CreateResourceRequest<AuthContext, InputType>,
        modelOps: DomainModelOps<AuthContext, IdType, *>
    ): UAPIResponse<*> {
        val ctx = CreateContextImpl(request.authContext, request.input)
        val authorized = authorization.invoke(ctx)
        if (!authorized) {
            return ErrorResponse.notAuthorized()
        }

        val createdId = handle.invoke(ctx)
        return modelOps.idToResult(request.authContext, createdId)
    }
}

internal data class CreateContextImpl<AuthContext, InputType>(
    override val authContext: AuthContext,
    override val input: InputType
) : CreateContext<AuthContext, InputType>
