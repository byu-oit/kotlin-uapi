package edu.byu.uapi.server.subresources.list

import edu.byu.uapi.server.subresources.ListSubresourceRuntime
import edu.byu.uapi.server.subresources.ParentResult
import edu.byu.uapi.server.subresources.RequestedFieldsetResponse
import edu.byu.uapi.server.subresources.SubresourceRequestContext
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.debug
import edu.byu.uapi.server.util.info
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.server.util.warn
import edu.byu.uapi.spi.input.ListParamReader
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.input.ListWithTotal
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.requests.ListSubresourceRequest
import edu.byu.uapi.spi.requests.QueryParams
import edu.byu.uapi.utility.takeIfType

sealed class ListSubresourceRequestHandler<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Params : ListParams, Request : ListSubresourceRequest<UserContext>>(
    val runtime: ListSubresourceRuntime<UserContext, Parent, Id, Model, Params>
) {
    val resource = runtime.subresource

    internal fun buildResponse(
        userContext: UserContext,
        parent: Parent,
        model: Model,
        validationResponse: ValidationResponse = ValidationResponse.OK,
        id: Id = resource.idFromModel(model)
    ): UAPIPropertiesResponse {
        return UAPIPropertiesResponse(
            properties = modelToProperties(userContext, parent, id, model),
            links = generateLinks(userContext, parent, id, model),
            metadata = UAPIResourceMeta(validationResponse)
        )
    }

    internal fun modelToProperties(
        userContext: UserContext,
        parent: Parent,
        id: Id,
        model: Model
    ): Map<String, UAPIProperty> {
        return resource.responseFields.map { f ->
            f.name to f.toProp(userContext, model)
        }.toMap()
    }

    @Suppress("UNUSED_PARAMETER")
    internal fun generateLinks(
        userContext: UserContext,
        parent: Parent,
        id: Id,
        model: Model
    ): UAPILinks {
        //TODO generate links
        return emptyMap()
    }

    fun getId(idParams: IdParams): Id {
        return runtime.idReader.read(idParams)
    }

    fun handle(request: Request): UAPIResponse<*> {
        val fieldsets = when (val r = runtime.parent.getRequestedFieldsets(request.requestContext.fieldsets)) {
            is RequestedFieldsetResponse.Success -> r.fieldsets
            RequestedFieldsetResponse.InvalidFieldsets -> return UAPIBadRequestError("Requested one or more invalid fieldsets.")
            RequestedFieldsetResponse.InvalidContexts -> return UAPIBadRequestError("Requested one or more invalid contexts.")
        }
        val context = SubresourceRequestContext.Simple(fieldsets)
        return when (val parent = runtime.parent.getParentModel(context, request.userContext, request.parentIdParams)) {
            is ParentResult.Success -> handle(request, context, parent.value)
            ParentResult.DoesNotExist -> UAPINotFoundError
            ParentResult.NotAuthorized -> UAPINotAuthorizedError
        }
    }

    abstract fun handle(
        request: Request,
        requestContext: SubresourceRequestContext,
        parent: Parent
    ): UAPIResponse<*>
}

class ListSubresourceFetchHandler<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Params : ListParams>(
    runtime: ListSubresourceRuntime<UserContext, Parent, Id, Model, Params>
) : ListSubresourceRequestHandler<UserContext, Parent, Id, Model, Params, ListSubresourceRequest.Fetch<UserContext>>(runtime) {
    override fun handle(
        request: ListSubresourceRequest.Fetch<UserContext>,
        requestContext: SubresourceRequestContext,
        parent: Parent
    ): UAPIResponse<*> {
        val id = getId(request.idParams)
        val userContext = request.userContext

        val model = resource.loadModel(requestContext, userContext, parent, id) ?: return UAPINotFoundError
        if (!resource.canUserViewModel(requestContext, userContext, parent, id, model)) {
            return UAPINotAuthorizedError
        }
        return buildResponse(
            userContext = userContext,
            parent = parent,
            id = id,
            model = model
        )
    }
}

class ListSubresourceListHandler<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Params : ListParams>(
    runtime: ListSubresourceRuntime<UserContext, Parent, Id, Model, Params>
) : ListSubresourceRequestHandler<UserContext, Parent, Id, Model, Params, ListSubresourceRequest.List<UserContext>>(runtime) {
    private val paramReader: ListParamReader<Params> = resource.getListParamReader(runtime.typeDictionary)

    override fun handle(
        request: ListSubresourceRequest.List<UserContext>,
        requestContext: SubresourceRequestContext,
        parent: Parent
    ): UAPIResponse<*> {
        return handle(requestContext, request.userContext, request.queryParams, parent)
    }

    fun handle(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        queryParams: QueryParams,
        parent: Parent
    ): UAPIResponse<*> {
        val params = paramReader.read(queryParams)

        val result = resource.list(requestContext, userContext, parent, params)

        val meta = buildCollectionMetadata(result, params)

        return UAPISubresourceCollectionResponse(
            result.map { buildResponse(userContext, parent, it) },
            meta,
            emptyMap() //TODO: Links
        )
    }

    private fun buildCollectionMetadata(
        list: List<Model>,
        params: Params
    ): CollectionMetadata {
        val meta = paramReader.describe()

        val size = if (list is ListWithTotal<Model>) list.totalItems else list.size

        val (search, _, sort, subset) = meta
        val searchMeta = search?.let { SearchableCollectionMetadata(it.contextFields) }
        val sortMeta = sort?.let { SortableCollectionMetadata(it.properties, it.defaults, it.defaultSortOrder) }
        val subsetMeta = subset?.let {
            params as ListParams.WithSubset
            CollectionSubsetMetadata(
                list.size,
                params.subset.subsetStartOffset,
                it.defaultSize,
                it.maxSize
            )
        }

        return CollectionMetadata(
            collectionSize = size,
            searchMetadata = searchMeta,
            sortMetadata = sortMeta,
            subsetMetadata = subsetMeta
        )
    }
}

class ListSubresourceCreateHandler<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Params : ListParams, Input : Any>(
    runtime: ListSubresourceRuntime<UserContext, Parent, Id, Model, Params>,
    private val operation: ListSubresource.Creatable<UserContext, Parent, Id, Model, Input>
) : ListSubresourceRequestHandler<UserContext, Parent, Id, Model, Params, ListSubresourceRequest.Create<UserContext>>(runtime) {

    companion object {
        private val LOG = loggerFor<ListSubresourceCreateHandler<*, *, *, *, *, *>>()
    }

    private val inputType = operation.createInput

    override fun handle(
        request: ListSubresourceRequest.Create<UserContext>,
        requestContext: SubresourceRequestContext,
        parent: Parent
    ): UAPIResponse<*> {
        LOG.debug { "Got request to create ${resource.singleName}" }
        val userContext = request.userContext

        val authorized = operation.canUserCreate(requestContext, userContext, parent)
        if (!authorized) {
            LOG.warn { "Unauthorized request to create ${resource.singleName}! User Context was $userContext" }
            return UAPINotAuthorizedError
        }
        val input = request.body.readAs(inputType)

        val validator = operation.getCreateValidator(runtime.validationEngine)
        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            LOG.warn { "Invalid create ${resource.singleName} request body: ${validationResponse.map { "${it.field}: ${it.should}" }}" }
            return GenericUAPIErrorResponse(
                statusCode = 400,
                message = "Bad Request",
                validationInformation = validationResponse.map { "The value for ${it.field} is invalid: ${it.should}" }
            )
        }
        return when (val result = operation.handleCreate(requestContext, userContext, parent, input)) {
            is CreateResult.Success -> {
                val model = result.model
                val id = resource.idFromModel(model)
                LOG.info { "Successfully created ${resource.singleName} $id" }
                super.buildResponse(
                    userContext = userContext,
                    id = id,
                    parent = parent,
                    model = model,
                    validationResponse = ValidationResponse(201, "Created")
                )
            }
            CreateResult.Unauthorized -> {
                LOG.warn { "Unauthorized request to create ${resource.singleName} (caught in handleCreate)! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is CreateResult.InvalidInput -> {
                LOG.warn { "Invalid create ${resource.singleName} request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                GenericUAPIErrorResponse(
                    400, "Bad Request", result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            is CreateResult.Error -> {
                LOG.error("Error(s) creating ${resource.singleName}: ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    result.code, "Error", result.errors
                )
            }
        }
    }
}

class ListSubresourceUpdateHandler<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Params : ListParams, Input : Any>(
    runtime: ListSubresourceRuntime<UserContext, Parent, Id, Model, Params>,
    private val operation: ListSubresource.Updatable<UserContext, Parent, Id, Model, Input>
) : ListSubresourceRequestHandler<UserContext, Parent, Id, Model, Params, ListSubresourceRequest.Update<UserContext>>(runtime) {

    companion object {
        private val LOG = loggerFor<ListSubresourceCreateHandler<*, *, *, *, *, *>>()
    }

    private val inputType = operation.updateInput
    private val createWithId = operation.takeIfType<ListSubresource.CreatableWithId<UserContext, Parent, Id, Model, Input>>()
    private val validator = operation.getUpdateValidator(runtime.validationEngine)

    override fun handle(
        request: ListSubresourceRequest.Update<UserContext>,
        requestContext: SubresourceRequestContext,
        parent: Parent
    ): UAPIResponse<*> {
        LOG.debug { "Got request to update ${resource.singleName}" }
        val userContext = request.userContext

        val id = getId(request.idParams)
        val input = request.body.readAs(inputType)

        val model = resource.loadModel(requestContext, userContext, parent, id)

        return when {
            model != null -> {
                val result = doUpdate(requestContext, userContext, parent, id, model, input)
                handleUpdateResult(userContext, parent, id, result)
            }
            createWithId != null -> {
                val result = doCreate(createWithId, requestContext, userContext, parent, id, input)
                handleCreateResult(userContext, parent, id, result)
            }
            else -> UAPINotFoundError
        }
    }

    private fun handleUpdateResult(
        userContext: UserContext,
        parent: Parent,
        id: Id,
        result: UpdateResult<Model>
    ): UAPIResponse<*> {
        return when (result) {
            is UpdateResult.Success -> {
                val model = result.model
                LOG.info { "Successfully updated ${resource.singleName} $id" }
                super.buildResponse(
                    userContext = userContext,
                    parent = parent,
                    id = id,
                    model = model,
                    validationResponse = ValidationResponse(200, "OK")
                )
            }
            is UpdateResult.InvalidInput -> {
                LOG.warn { "Invalid update ${resource.singleName} $id request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                GenericUAPIErrorResponse(
                    400, "Bad Request", result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            UpdateResult.Unauthorized -> {
                LOG.warn { "Unauthorized request to update ${resource.singleName} $id! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is UpdateResult.CannotBeUpdated -> {
                LOG.warn { "Got request to update ${resource.singleName} $id, but updates are not allowed: ${result.reason}" }
                GenericUAPIErrorResponse(
                    statusCode = 409,
                    message = "Conflict",
                    validationInformation = listOf(result.reason)
                )
            }
            is UpdateResult.Error -> {
                LOG.warn("Unknown error updating ${resource.singleName} $id: code ${result.code} ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    statusCode = result.code,
                    message = "Error",
                    validationInformation = result.errors
                )
            }
        }
    }

    private fun doUpdate(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        id: Id,
        model: Model,
        input: Input
    ): UpdateResult<Model> {
        val authorized = operation.canUserUpdate(requestContext, userContext, parent, id, model)
        if (!authorized) {
            return UpdateResult.Unauthorized
        }

        val canBeUpdated = operation.canBeUpdated(parent, id, model)
        if (!canBeUpdated) {
            return UpdateResult.CannotBeUpdated("cannot be updated at this time.")
        }

        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            return UpdateResult.InvalidInput(validationResponse.map { InputError(it.field, it.should) })
        }
        return operation.handleUpdate(requestContext, userContext, parent, id, model, input)
    }

    private fun doCreate(
        operation: ListSubresource.CreatableWithId<UserContext, Parent, Id, Model, Input>,
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        id: Id,
        input: Input
    ): CreateResult<Model> {
        val authorized = operation.canUserCreateWithId(requestContext, userContext, parent, id)
        if (!authorized) {
            return CreateResult.Unauthorized
        }

        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            return CreateResult.InvalidInput(validationResponse.map { InputError(it.field, it.should) })
        }
        return operation.handleCreateWithId(requestContext, userContext, parent, id, input)
    }

    private fun handleCreateResult(
        userContext: UserContext,
        parent: Parent,
        id: Id,
        result: CreateResult<Model>
    ): UAPIResponse<*> {
        return when (result) {
            is CreateResult.Success -> {
                LOG.info { "Successfully created ${resource.singleName} $id" }
                super.buildResponse(
                    userContext = userContext,
                    parent = parent,
                    id = id,
                    model = result.model,
                    validationResponse = ValidationResponse(201, "Created")
                )
            }
            CreateResult.Unauthorized -> {
                LOG.warn { "Unauthorized request to create ${resource.singleName} $id! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is CreateResult.InvalidInput -> {
                LOG.warn { "Invalid create ${resource.singleName} $id request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                UAPIBadRequestError(
                    result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            is CreateResult.Error -> {
                LOG.error("Error(s) creating ${resource.singleName} $id: ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    result.code, "Error", result.errors
                )
            }
        }
    }
}

class ListSubresourceDeleteHandler<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Params : ListParams>(
    runtime: ListSubresourceRuntime<UserContext, Parent, Id, Model, Params>,
    val operation: ListSubresource.Deletable<UserContext, Parent, Id, Model>
) : ListSubresourceRequestHandler<UserContext, Parent, Id, Model, Params, ListSubresourceRequest.Delete<UserContext>>(runtime) {
    companion object {
        private val LOG = loggerFor<ListSubresourceDeleteHandler<*, *, *, *, *>>()
    }

    override fun handle(
        request: ListSubresourceRequest.Delete<UserContext>,
        requestContext: SubresourceRequestContext,
        parent: Parent
    ): UAPIResponse<*> {
        val id = getId(request.idParams)
        val userContext = request.userContext

        val model = resource.loadModel(requestContext, userContext, parent, id)

        val result = doDelete(requestContext, userContext, parent, id, model)

        return when (result) {
            DeleteResult.Success -> {
                LOG.info("Successfully deleted ${resource.singleName} $id")
                UAPIEmptyResponse
            }
            DeleteResult.AlreadyDeleted -> {
                LOG.info("${resource.singleName} $id has already been deleted; returning success")
                UAPIEmptyResponse
            }
            DeleteResult.Unauthorized -> {
                LOG.warn("Unauthorized request to delete ${resource.singleName} $id! User contexts was $userContext")
                UAPINotAuthorizedError
            }
            is DeleteResult.CannotBeDeleted -> {
                LOG.warn("${resource.singleName} $id cannot be deleted: ${result.reason}")
                GenericUAPIErrorResponse(
                    statusCode = 409,
                    message = "Conflict",
                    validationInformation = listOf(result.reason)
                )
            }
            is DeleteResult.Error -> {
                LOG.error("Unexpected error deleting ${resource.singleName} $id: ${result.code} ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    statusCode = result.code,
                    message = "Error",
                    validationInformation = result.errors
                )
            }
        }
    }

    private fun doDelete(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        id: Id,
        model: Model?
    ): DeleteResult {
        if (model == null) {
            return DeleteResult.AlreadyDeleted
        }
        if (!operation.canUserDelete(requestContext, userContext, parent, id, model)) {
            return DeleteResult.Unauthorized
        }
        if (!operation.canBeDeleted(parent, id, model)) {
            return DeleteResult.CannotBeDeleted("Cannot be deleted")
        }
        return operation.handleDelete(requestContext, userContext, parent, id, model)
    }
}
