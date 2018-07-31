package edu.byu.uapidsl.model.resource.identified.ops

import edu.byu.uapidsl.model.resource.*
import edu.byu.uapidsl.model.resource.identified.*
import edu.byu.uapidsl.types.ErrorResponse
import edu.byu.uapidsl.types.UAPIResponse
import either.Either
import either.Left
import either.Right

sealed class UpdateOperation<Auth, Id, Model, Input : Any, RequestContext> {
    abstract val input: InputModel<Input>
    abstract val authorized: Authorizer<RequestContext>
    abstract val handle: Handler<RequestContext>

    fun handleRequest(
        request: UpdateResourceRequest<Auth, Id, Input>,
        modelOps: DomainModelOps<Auth, Id, Model>
    ): UAPIResponse<*> {
        val auth = request.authContext
        val body = request.input
        val id = request.id
        val model = modelOps.idToModel(auth, id)

        val contextResult = createRequestContext(auth, id, model, body)

        val context: RequestContext = when (contextResult) {
            is Left -> contextResult.value
            is Right -> return@handleRequest contextResult.value
        }

        val authorized = authorized.invoke(context)
        if (!authorized) {
            return ErrorResponse.notAuthorized()
        }

        handle.invoke(context)

        //Load the created/updated resource again
        return modelOps.idToResult(auth, id)
    }

    abstract fun createRequestContext(
        authContext: Auth,
        id: Id,
        model: Model?,
        body: Input
    ): Either<RequestContext, UAPIResponse<*>>
}

internal typealias IdentifiedUpdateContext<Auth, Id, Model, Input> =
    ResourceUpdateContext<Auth, Model, Input, IdentifiedResourceModelContext<Id, Model>>

internal typealias IdentifiedUpdateValidationContext<Auth, Id, Model, Input> =
    ResourceUpdateValidationContext<Auth, Model, Input, IdentifiedResourceModelContext<Id, Model>>

data class SimpleUpdateOperation<Auth : Any, Id : Any, Model : Any, Input : Any>(
    override val input: InputModel<Input>,
    override val authorized: Authorizer<IdentifiedUpdateContext<Auth, Id, Model, Input>>,
    override val handle: Handler<IdentifiedUpdateContext<Auth, Id, Model, Input>>,
    override val validate: Validator<IdentifiedUpdateValidationContext<Auth, Id, Model, Input>>
) : UpdateOperation<Auth, Id, Model, Input,
    IdentifiedUpdateContext<Auth, Id, Model, Input>>(),
    ResourceUpdateOperation<Auth, Model, Input, IdentifiedResourceModelContext<Id, Model>> {

    override fun createRequestContext(authContext: Auth, id: Id, model: Model?, body: Input): Either<IdentifiedUpdateContext<Auth, Id, Model, Input>, UAPIResponse<*>> {
        if (model == null) {
            return Right(ErrorResponse.notFound())
        }
        return Left(ResourceUpdateContext.Default(
            authContext,
            IdentifiedResourceModelContext(id, model),
            body
        ))
    }
}

internal typealias IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input> =
    ResourceCreateOrUpdateContext<Auth, Model, Input, IdentifiedResourceOptionalModelContext<Id, Model>>
internal typealias IdentifiedCreateOrUpdateValidationContext<Auth, Id, Model, Input> =
    ResourceCreateOrUpdateValidationContext<Auth, Model, Input, IdentifiedResourceOptionalModelContext<Id, Model>>

data class CreateOrUpdateOperation<Auth: Any, Id: Any, Model: Any, Input : Any>(
    override val input: InputModel<Input>,
    override val authorized: Authorizer<IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input>>,
    override val handle: Handler<IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input>>,
    override val validate: Validator<IdentifiedCreateOrUpdateValidationContext<Auth, Id, Model, Input>>
) : UpdateOperation<Auth, Id, Model, Input,
    IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input>>(),
ResourceCreateOrUpdateOperation<Auth, Model, Input, IdentifiedResourceOptionalModelContext<Id, Model>>{

    override fun createRequestContext(
        authContext: Auth,
        id: Id,
        model: Model?,
        body: Input
    ): Either<IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input>, UAPIResponse<*>> {
        return Left(ResourceCreateOrUpdateContext.Default(authContext, IdentifiedResourceOptionalModelContext(id, model), body))
    }
}

