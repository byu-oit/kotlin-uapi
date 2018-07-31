package edu.byu.uapidsl.model.resource.identified.ops

import edu.byu.uapidsl.model.resource.Authorizer
import edu.byu.uapidsl.model.resource.Validator
import edu.byu.uapidsl.model.resource.identified.*
import edu.byu.uapidsl.types.ErrorResponse
import edu.byu.uapidsl.types.UAPIResponse

data class CreateOperation<Auth, Id, Input : Any>(
    val input: InputModel<Input>,
    val authorization: Authorizer<CreateContext<Auth, Input>>,
    val handle: CreateHandler<Auth, Id, Input>,
    val validator: Validator<CreateValidationContext<Auth, Input>>
) {
    fun handleRequest(
        request: CreateResourceRequest<Auth, Input>,
        modelOps: DomainModelOps<Auth, Id, *>
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

internal data class CreateContextImpl<Auth, Input>(
    override val authContext: Auth,
    override val input: Input
) : CreateContext<Auth, Input>
