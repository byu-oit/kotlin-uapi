package edu.byu.uapi.server.subresources.singleton

import edu.byu.uapi.server.subresources.ParentResult
import edu.byu.uapi.server.subresources.SubresourceParent
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.debug
import edu.byu.uapi.server.util.info
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.server.util.warn
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.validation.ValidationEngine
import edu.byu.uapi.utility.takeIfType
import java.util.*

sealed class SubresourceRuntime<UserContext : Any, Parent : ModelHolder, Model : Any>

class SingletonSubresourceRuntime<UserContext : Any, Parent : ModelHolder, Model : Any>(
    internal val subresource: SingletonSubresource<UserContext, Parent, Model>,
    val parent: SubresourceParent<UserContext, Parent>,
    val typeDictionary: TypeDictionary,
    val validationEngine: ValidationEngine
) : SubresourceRuntime<UserContext, Parent, Model>() {
    val name = subresource.name

    companion object {
        private val LOG = loggerFor<SingletonSubresourceRuntime<*, *, *>>()
    }

    init {
        LOG.debug("Initializing $name runtime")
    }

    val availableOperations: Set<SingletonSubresourceRequestHandler<UserContext, Parent, Model, *>> by lazy {
        val ops: MutableSet<SingletonSubresourceRequestHandler<UserContext, Parent, Model, *>> = mutableSetOf()

        ops.add(SingletonSubresourceFetchHandler(this))

        subresource.updateMutation?.also { ops.add(SingletonSubresourceUpdateHandler(this, it)) }
        subresource.deleteMutation?.also { ops.add(SingletonSubresourceDeleteHandler(this, it)) }

        Collections.unmodifiableSet(ops)
    }

}


sealed class SingletonSubresourceRequestHandler<UserContext : Any, Parent : ModelHolder, Model : Any, Request : SingletonSubresourceRequest<UserContext>>(
) {
    abstract val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>
    val subresource: SingletonSubresource<UserContext, Parent, Model>
        get() = runtime.subresource

    fun handle(request: Request): UAPIResponse<*> {
        return when (val parent = runtime.parent.getParentModel(request.userContext, request.parentParams)) {
            is ParentResult.Success -> handle(request, parent.value)
            ParentResult.DoesNotExist -> UAPINotFoundError
            ParentResult.NotAuthorized -> UAPINotAuthorizedError
        }
    }

    abstract fun handle(
        request: Request,
        parent: Parent
    ): UAPIResponse<*>

    internal fun buildResponse(
        user: UserContext,
        parent: Parent,
        model: Model,
        validationResponse: ValidationResponse = ValidationResponse.OK
    ): UAPIPropertiesResponse {
        return UAPIPropertiesResponse(
            metadata = UAPIResourceMeta(validationResponse),
            links = generateLinks(user, parent, model),
            properties = modelToProperties(user, parent, model)
        )
    }

    private fun modelToProperties(
        user: UserContext,
        parent: Parent,
        model: Model
    ): Map<String, UAPIProperty> {
        return subresource.responseFields.map { f ->
            f.name to f.toProp(user, model)
        }.toMap()
    }

    @Suppress("UNUSED_PARAMETER")
    internal fun generateLinks(
        userContext: UserContext,
        parent: Parent,
        model: Model
    ): UAPILinks {
        //TODO generate links
        return emptyMap()
    }

}

class SingletonSubresourceFetchHandler<UserContext : Any, Parent : ModelHolder, Model : Any>(
    override val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>
) : SingletonSubresourceRequestHandler<UserContext, Parent, Model, FetchSingletonSubresource<UserContext>>() {
    override fun handle(
        request: FetchSingletonSubresource<UserContext>,
        parent: Parent
    ): UAPIResponse<*> {
        val user = request.userContext

        val model = subresource.loadModel(user, parent) ?: return UAPINotFoundError

        if (!subresource.canUserViewModel(user, parent, model)) {
            return UAPINotAuthorizedError
        }
        return buildResponse(user, parent, model)
    }
}

class SingletonSubresourceDeleteHandler<UserContext : Any, Parent : ModelHolder, Model : Any>(
    override val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>,
    val mutation: SingletonSubresource.Deletable<UserContext, Parent, Model>
) : SingletonSubresourceRequestHandler<UserContext, Parent, Model, DeleteSingletonSubresource<UserContext>>() {
    companion object {
        private val LOG = loggerFor<SingletonSubresourceDeleteHandler<*, *, *>>()
    }

    override fun handle(
        request: DeleteSingletonSubresource<UserContext>,
        parent: Parent
    ): UAPIResponse<*> {
        val user = request.userContext
        val model = subresource.loadModel(user, parent)

        return when (val result = doDelete(user, parent, model)) {
            DeleteResult.Success -> {
                LOG.info("Successfully deleted ${subresource.name}")
                UAPIEmptyResponse
            }
            DeleteResult.AlreadyDeleted -> {
                LOG.info("${subresource.name} has already been deleted; returning success")
                UAPIEmptyResponse
            }
            DeleteResult.Unauthorized -> {
                LOG.warn("Unauthorized request to delete ${subresource.name}! User context was $user")
                UAPINotAuthorizedError
            }
            is DeleteResult.CannotBeDeleted -> {
                LOG.warn("${subresource.name} cannot be deleted: ${result.reason}")
                GenericUAPIErrorResponse(
                    statusCode = 409,
                    message = "Conflict",
                    validationInformation = listOf(result.reason)

                )
            }
            is DeleteResult.Error -> {
                LOG.error("Unexpected error deleting ${subresource.name}: ${result.code} ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    statusCode = result.code,
                    message = "Error",
                    validationInformation = result.errors
                )
            }
        }
    }

    private fun doDelete(
        user: UserContext,
        parent: Parent,
        model: Model?
    ): DeleteResult {
        if (model == null) {
            return DeleteResult.AlreadyDeleted
        }
        if (!mutation.canUserDelete(user, parent, model)) {
            return DeleteResult.Unauthorized
        }
        if (!mutation.canBeDeleted(parent, model)) {
            return DeleteResult.CannotBeDeleted("Cannot be deleted")
        }
        return mutation.handleDelete(user, parent, model)
    }
}



class SingletonSubresourceUpdateHandler<UserContext : Any, Parent: ModelHolder, Model : Any, Input : Any>(
    override val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>,
    private val operation: SingletonSubresource.Updatable<UserContext, Parent, Model, Input>
) : SingletonSubresourceRequestHandler<UserContext, Parent, Model, UpdateSingletonSubresource<UserContext>>() {

    companion object {
        private val LOG = loggerFor<SingletonSubresourceUpdateHandler<*, *, *, *>>()
    }

    private val inputType = operation.updateInput
    private val createWithId = operation.takeIfType<SingletonSubresource.Creatable<UserContext, Parent, Model, Input>>()
    private val validator = operation.getUpdateValidator(runtime.validationEngine)

    override fun handle(request: UpdateSingletonSubresource<UserContext>, parent: Parent): UAPIResponse<*> {
        LOG.debug { "Got request to update ${subresource.name}" }
        val userContext = request.userContext

        val input = request.body.readAs(inputType)
        val model = subresource.loadModel(userContext, parent)

        return when {
            model != null -> {
                val result = doUpdate(userContext, parent, model, input)
                handleUpdateResult(userContext, parent, result)
            }
            createWithId != null -> {
                val result = doCreate(createWithId, userContext, parent, input)
                handleCreateResult(userContext, parent, result)
            }
            else -> UAPINotFoundError
        }
    }

    private fun handleUpdateResult(
        userContext: UserContext,
        parent: Parent,
        result: UpdateResult<Model>
    ): UAPIResponse<*> {
        return when (result) {
            is UpdateResult.Success -> {
                LOG.info { "Successfully updated ${subresource.name}" }
                buildResponse(userContext, parent, result.model)
            }
            is UpdateResult.InvalidInput -> {
                LOG.warn { "Invalid update ${subresource.name} request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                GenericUAPIErrorResponse(
                    400, "Bad Request", result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            UpdateResult.Unauthorized -> {
                LOG.warn { "Unauthorized request to update ${subresource.name}! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is UpdateResult.CannotBeUpdated -> {
                LOG.warn { "Got request to update ${subresource.name}, but updates are not allowed: ${result.reason}" }
                GenericUAPIErrorResponse(
                    statusCode = 409,
                    message = "Conflict",
                    validationInformation = listOf(result.reason)
                )
            }
            is UpdateResult.Error -> {
                LOG.warn("Unknown error udpating ${subresource.name}: code ${result.code} ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    statusCode = result.code,
                    message = "Error",
                    validationInformation = result.errors
                )
            }
        }
    }

    private fun doUpdate(
        userContext: UserContext,
        parent: Parent,
        model: Model,
        input: Input
    ): UpdateResult<Model> {
        val authorized = operation.canUserUpdate(userContext, parent, model)
        if (!authorized) {
            return UpdateResult.Unauthorized
        }

        val canBeUpdated = operation.canBeUpdated(parent, model)
        if (!canBeUpdated) {
            return UpdateResult.CannotBeUpdated("cannot be updated at this time.")
        }

        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            return UpdateResult.InvalidInput(validationResponse.map { InputError(it.field, it.should) })
        }
        return operation.handleUpdate(userContext, parent, model, input)
    }

    private fun doCreate(
        operation: SingletonSubresource.Creatable<UserContext, Parent, Model, Input>,
        userContext: UserContext,
        parent: Parent,
        input: Input
    ): CreateResult<Model> {
        val authorized = operation.canUserCreate(userContext, parent)
        if (!authorized) {
            return CreateResult.Unauthorized
        }

        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            return CreateResult.InvalidInput(validationResponse.map { InputError(it.field, it.should) })
        }
        return operation.handleCreate(userContext, parent, input)
    }

    private fun handleCreateResult(
        userContext: UserContext,
        parent: Parent,
        result: CreateResult<Model>
    ): UAPIResponse<*> {
        return when (result) {
            is CreateResult.Success -> {
                LOG.info { "Successfully created ${subresource.name}" }
                buildResponse(userContext, parent, result.model)
            }
            CreateResult.Unauthorized -> {
                LOG.warn { "Unauthorized request to create ${subresource.name}! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is CreateResult.InvalidInput -> {
                LOG.warn { "Invalid create ${subresource.name} request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                UAPIBadRequestError(
                    result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            is CreateResult.Error -> {
                LOG.error("Error(s) creating ${subresource.name}: ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    result.code, "Error", result.errors
                )
            }
        }
    }
}


