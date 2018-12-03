package edu.byu.uapi.server.resources.list

import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.server.schemas.*
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.debug
import edu.byu.uapi.server.util.info
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.server.util.warn
import edu.byu.uapi.spi.SpecConstants
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ListParamReader
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.input.ListWithTotal
import edu.byu.uapi.spi.requests.*
import edu.byu.uapi.spi.validation.ValidationEngine
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class ListResourceRuntime<UserContext : Any, Id : Any, Model : Any, Params: ListParams>(
    val name: String,
    internal val resource: ListResource<UserContext, Id, Model, Params>,
    val typeDictionary: TypeDictionary,
    val validationEngine: ValidationEngine
) {

    // TODO fun validateResource(validation: Validating)

    companion object {
        private val LOG = loggerFor<ListResourceRuntime<*, *, *, *>>()
    }

    val idReader: IdParamReader<Id> = resource.getIdReader(typeDictionary, this.name + "_")

    init {
        LOG.debug("Initializing runtime")
    }

    val model: ListResourceModel by lazy {
        introspect(this)
    }

    val availableOperations: Set<ListResourceRequestHandler<UserContext, Id, Model, Params, *>> by lazy {
        val ops: MutableSet<ListResourceRequestHandler<UserContext, Id, Model, Params, *>> = mutableSetOf()

        ops.add(ListResourceFetchHandler(this))
        ops.add(ListResourceListHandler(this))

        resource.createOperation?.also { ops.add(ListResourceCreateHandler(this, it)) }
        resource.updateOperation?.also { ops.add(ListResourceUpdateHandler(this, it)) }
        resource.deleteOperation?.also { ops.add(ListResourceDeleteHandler(this, it)) }

        Collections.unmodifiableSet(ops)
    }

    val availableFieldsets = setOf(SpecConstants.FieldSets.VALUE_BASIC)
    val availableContexts = emptyMap<String, Set<String>>()

}

inline fun <reified T> Any.takeIfType(): T? {
    return if (this is T) {
        this
    } else {
        null
    }
}

private fun introspect(runtime: ListResourceRuntime<*, *, *, *>): ListResourceModel {
    val resource = runtime.resource
    val name = runtime.name

    TODO()
//    return ListResourceModel(
//        name = name,
//        identifier = introspectIdentifier(name, resource.idType),
//        responseModel = introspectResponseModel(resource.responseFields),
//        listViewModel = introspectListView(resource.listView),
//        mutations = ListResourceMutations(
//            introspect(resource.createOperation),
//            introspect(resource.updateOperation),
//            introspect(resource.deleteOperation)
//        )
//    )
}

fun introspect(deleteOperation: ListResource.Deletable<*, *, *>?): DeleteOperationModel? {
    TODO("not implemented")
}

fun introspect(
    updateOperation: ListResource.Updatable<*, *, *, *>?
): UpdateOperationModel? {
    TODO()
}

fun introspect(runtime: ListResource.Creatable<*, *, *, *>?): CreateOperationModel? {
    TODO()
}

private fun introspectResponseModel(responseFields: List<ResponseField<*, *, *>>): ResponseModel {
    TODO()
}

private fun introspectIdentifier(
    parentName: String,
    idType: KClass<out Any>
): IdentifierModel {
    return if (idType.isData) {
        val fields = idType.primaryConstructor!!.parameters.map {
            val type = scalarTypeFor(it.type)
                ?: throw IllegalStateException("invalid ID Type for $parentName.${it.name}: ${it.type} is not a UAPI scalar type or a data class")
            IdentifierField(it.name!!, type)
        }
        IdentifierModel(fields)
    } else {
        val type = scalarTypeFor(idType)
            ?: throw IllegalStateException("Invalid ID Type for $parentName: $idType is not a UAPI scalar type or a data class")
        IdentifierModel(listOf(IdentifierField("$parentName _id", type)))
    }
}

private fun scalarTypeFor(type: KType): UAPIScalarType? {
    val classifier = type.classifier as? KClass<*> ?: return null
    return scalarTypeFor(classifier)
}

private fun scalarTypeFor(type: KClass<out Any>): UAPIScalarType? {
    val primitive = type.javaPrimitiveType
    if (primitive != null) {
        return when (primitive) {
            PrimitiveTypes.SHORT -> UAPIScalarType.NUMBER
            PrimitiveTypes.INT -> UAPIScalarType.NUMBER
            PrimitiveTypes.LONG -> UAPIScalarType.NUMBER
            PrimitiveTypes.FLOAT -> UAPIScalarType.NUMBER
            PrimitiveTypes.DOUBLE -> UAPIScalarType.NUMBER
            PrimitiveTypes.BOOLEAN -> UAPIScalarType.BOOLEAN
            else -> null
        }
    }
    return when (type) {
        String::class -> UAPIScalarType.STRING
        Enum::class -> UAPIScalarType.STRING

        Number::class -> UAPIScalarType.NUMBER

        Date::class -> UAPIScalarType.DATE
        LocalDate::class -> UAPIScalarType.DATE

        ZonedDateTime::class -> UAPIScalarType.DATE_TIME
        Instant::class -> UAPIScalarType.DATE_TIME

        Boolean::class -> UAPIScalarType.BOOLEAN
        else -> null
    }
}

sealed class ListResourceRequestHandler<UserContext : Any, Id : Any, Model : Any, Params: ListParams, Request : ListResourceRequest<UserContext>>(
    val runtime: ListResourceRuntime<UserContext, Id, Model, Params>
) {
    val resource = runtime.resource

    internal fun idToBasic(
        userContext: UserContext,
        id: Id,
        validationResponse: ValidationResponse = ValidationResponse.OK
    ): UAPIPropertiesResponse {
        val model = resource.loadModel(userContext, id) ?: throw IllegalStateException() //TODO: Prettier error message
        return modelToBasic(userContext, id, model, validationResponse)
    }

    internal fun modelToBasic(
        userContext: UserContext,
        id: Id,
        model: Model,
        validationResponse: ValidationResponse = ValidationResponse.OK
    ): UAPIPropertiesResponse {
        return UAPIPropertiesResponse(
            metadata = UAPIResourceMeta(validationResponse = validationResponse),
            links = generateLinks(userContext, id, model),
            properties = modelToProperties(userContext, id, model)
        )
    }

    internal fun modelToProperties(
        userContext: UserContext,
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
        id: Id,
        model: Model
    ): UAPILinks {
        //TODO generate links
        return emptyMap()
    }

    internal fun buildFieldsetResponse(
        userContext: UserContext,
        id: Id,
        model: Model,
        requestedFieldsets: Set<String>,
        requestedContexts: Set<String>
    ): UAPIFieldsetsResponse {
        val loadedFieldsets = loadFieldsets(userContext, id, model, requestedFieldsets, requestedContexts)

        return UAPIFieldsetsResponse(
            fieldsets = loadedFieldsets,
            metadata = FieldsetsMetadata(
                fieldSetsReturned = loadedFieldsets.keys,
                fieldSetsAvailable = runtime.availableFieldsets
            )
        )
    }

    internal fun loadFieldsets(
        userContext: UserContext,
        id: Id,
        model: Model,
        requestedFieldsets: Set<String>,
        requestedContexts: Set<String>
    ): Map<String, UAPIResponse<*>> {
        //TODO(Return fieldsets other than basic)
        return mapOf(SpecConstants.FieldSets.VALUE_BASIC to modelToBasic(userContext, id, model))
    }

    fun getId(idParams: IdParams): Id {
        return runtime.idReader.read(idParams)
    }

    abstract fun handle(request: Request): UAPIResponse<*>
}

class ListResourceFetchHandler<UserContext : Any, Id : Any, Model : Any, Params: ListParams>(
    runtime: ListResourceRuntime<UserContext, Id, Model, Params>
) : ListResourceRequestHandler<UserContext, Id, Model, Params, FetchListResource<UserContext>>(runtime) {
    override fun handle(
        request: FetchListResource<UserContext>
    ): UAPIResponse<*> {
        val id = getId(request.idParams)
        val userContext = request.userContext

        val model = resource.loadModel(userContext, id) ?: return UAPINotFoundError
        if (!resource.canUserViewModel(userContext, id, model)) {
            return UAPINotAuthorizedError
        }
        return buildFieldsetResponse(userContext, id, model, runtime.availableFieldsets, setOf()) // TODO: Fieldsets
    }
}

class ListResourceListHandler<UserContext : Any, Id : Any, Model : Any, Params : ListParams>(
    runtime: ListResourceRuntime<UserContext, Id, Model, Params>
) : ListResourceRequestHandler<UserContext, Id, Model, Params, ListListResource<UserContext>>(runtime) {
    private val paramReader: ListParamReader<Params> = resource.getListParamReader(runtime.typeDictionary)

    override fun handle(request: ListListResource<UserContext>): UAPIResponse<*> {
        val params = paramReader.read(request.queryParams)

        val result = resource.list(request.userContext, params)

        val meta = buildCollectionMetadata(result, params)

        return UAPIFieldsetsCollectionResponse(
            result.map { buildFieldsetResponse(request.userContext, resource.idFromModel(it), it, runtime.availableFieldsets, setOf()) },
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

class ListResourceCreateHandler<UserContext : Any, Id : Any, Model : Any, Params: ListParams, Input : Any>(
    runtime: ListResourceRuntime<UserContext, Id, Model, Params>,
    private val operation: ListResource.Creatable<UserContext, Id, Model, Input>
) : ListResourceRequestHandler<UserContext, Id, Model, Params, CreateListResource<UserContext>>(runtime) {

    companion object {
        private val LOG = loggerFor<ListResourceCreateHandler<*, *, *, *, *>>()
    }

    private val inputType = operation.createInput

    override fun handle(request: CreateListResource<UserContext>): UAPIResponse<*> {
        LOG.debug { "Got request to create ${runtime.name}" }
        val userContext = request.userContext

        val authorized = operation.canUserCreate(userContext)
        if (!authorized) {
            LOG.warn { "Unauthorized request to create ${runtime.name}! User Context was $userContext" }
            return UAPINotAuthorizedError
        }
        val input = request.body.readAs(inputType)

        val validator = operation.getCreateValidator(runtime.validationEngine)
        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            LOG.warn { "Invalid create ${runtime.name} request body: ${validationResponse.map { "${it.field}: ${it.should}" }}" }
            return GenericUAPIErrorResponse(
                statusCode = 400,
                message = "Bad Request",
                validationInformation = validationResponse.map { "The value for ${it.field} is invalid: ${it.should}" }
            )
        }
        return when (val result = operation.handleCreate(userContext, input)) {
            is CreateResult.Success -> {
                LOG.info { "Successfully created ${runtime.name} ${result.id}" }
                super.idToBasic(
                    userContext = userContext,
                    id = result.id,
                    validationResponse = ValidationResponse(201, "Created")
                )
            }
            CreateResult.Unauthorized -> {
                LOG.warn { "Unauthorized request to create ${runtime.name} (caught in handleCreate)! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is CreateResult.InvalidInput -> {
                LOG.warn { "Invalid create ${runtime.name} request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                GenericUAPIErrorResponse(
                    400, "Bad Request", result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            is CreateResult.Error -> {
                LOG.error("Error(s) creating ${runtime.name}: ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    result.code, "Error", result.errors
                )
            }
        }
    }
}

class ListResourceUpdateHandler<UserContext : Any, Id : Any, Model : Any, Params: ListParams, Input : Any>(
    runtime: ListResourceRuntime<UserContext, Id, Model, Params>,
    private val operation: ListResource.Updatable<UserContext, Id, Model, Input>
) : ListResourceRequestHandler<UserContext, Id, Model, Params, UpdateListResource<UserContext>>(runtime) {

    companion object {
        private val LOG = loggerFor<ListResourceCreateHandler<*, *, *, *, *>>()
    }

    private val inputType = operation.updateInput
    private val createWithId = operation.takeIfType<ListResource.CreatableWithId<UserContext, Id, Model, Input>>()
    private val validator = operation.getUpdateValidator(runtime.validationEngine)

    override fun handle(request: UpdateListResource<UserContext>): UAPIResponse<*> {
        LOG.debug { "Got request to update ${runtime.name}" }
        val userContext = request.userContext

        val id = getId(request.idParams)
        val input = request.body.readAs(inputType)

        val model = resource.loadModel(userContext, id)

        return when {
            model != null -> {
                val result = doUpdate(userContext, id, model, input)
                handleUpdateResult(userContext, id, result)
            }
            createWithId != null -> {
                val result = doCreate(createWithId, userContext, id, input)
                handleCreateResult(userContext, id, result)
            }
            else -> UAPINotFoundError
        }
    }

    private fun handleUpdateResult(
        userContext: UserContext,
        id: Id,
        result: UpdateResult
    ): UAPIResponse<*> {
        return when (result) {
            UpdateResult.Success -> {
                LOG.info { "Successfully updated ${runtime.name} $id" }
                super.idToBasic(
                    userContext = userContext,
                    id = id,
                    validationResponse = ValidationResponse(200, "OK")
                )
            }
            is UpdateResult.InvalidInput -> {
                LOG.warn { "Invalid update ${runtime.name} $id request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                GenericUAPIErrorResponse(
                    400, "Bad Request", result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            UpdateResult.Unauthorized -> {
                LOG.warn { "Unauthorized request to update ${runtime.name} $id! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is UpdateResult.CannotBeUpdated -> {
                LOG.warn { "Got request to update ${runtime.name} $id, but updates are not allowed: ${result.reason}" }
                GenericUAPIErrorResponse(
                    statusCode = 409,
                    message = "Conflict",
                    validationInformation = listOf(result.reason)
                )
            }
            is UpdateResult.Error -> {
                LOG.warn("Unknown error udpating ${runtime.name} $id: code ${result.code} ${result.errors.joinToString()}", result.cause)
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
        id: Id,
        model: Model,
        input: Input
    ): UpdateResult {
        val authorized = operation.canUserUpdate(userContext, id, model)
        if (!authorized) {
            return UpdateResult.Unauthorized
        }

        val canBeUpdated = operation.canBeUpdated(id, model)
        if (!canBeUpdated) {
            return UpdateResult.CannotBeUpdated("cannot be updated at this time.")
        }

        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            return UpdateResult.InvalidInput(validationResponse.map { InputError(it.field, it.should) })
        }
        return operation.handleUpdate(userContext, id, model, input)
    }

    private fun doCreate(
        operation: ListResource.CreatableWithId<UserContext, Id, Model, Input>,
        userContext: UserContext,
        id: Id,
        input: Input
    ): CreateWithIdResult {
        val authorized = operation.canUserCreateWithId(userContext, id)
        if (!authorized) {
            return CreateWithIdResult.Unauthorized
        }

        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            return CreateWithIdResult.InvalidInput(validationResponse.map { InputError(it.field, it.should) })
        }
        return operation.handleCreateWithId(userContext, id, input)
    }

    private fun handleCreateResult(
        userContext: UserContext,
        id: Id,
        result: CreateWithIdResult
    ): UAPIResponse<*> {
        return when (result) {
            is CreateWithIdResult.Success -> {
                LOG.info { "Successfully created ${runtime.name} $id" }
                super.idToBasic(
                    userContext = userContext,
                    id = id,
                    validationResponse = ValidationResponse(201, "Created")
                )
            }
            CreateWithIdResult.Unauthorized -> {
                LOG.warn { "Unauthorized request to create ${runtime.name} $id! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is CreateWithIdResult.InvalidInput -> {
                LOG.warn { "Invalid create ${runtime.name} $id request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                UAPIBadRequestError(
                    result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            is CreateWithIdResult.Error -> {
                LOG.error("Error(s) creating ${runtime.name} $id: ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    result.code, "Error", result.errors
                )
            }
        }
    }
}

class ListResourceDeleteHandler<UserContext : Any, Id : Any, Model : Any, Params: ListParams>(
    runtime: ListResourceRuntime<UserContext, Id, Model, Params>,
    val operation: ListResource.Deletable<UserContext, Id, Model>
) : ListResourceRequestHandler<UserContext, Id, Model, Params, DeleteListResource<UserContext>>(runtime) {
    companion object {
        private val LOG = loggerFor<ListResourceDeleteHandler<*, *, *, *>>()
    }

    override fun handle(
        request: DeleteListResource<UserContext>
    ): UAPIResponse<*> {
        val id = getId(request.idParams)
        val userContext = request.userContext

        val model = resource.loadModel(userContext, id)

        val result = doDelete(userContext, id, model)

        return when (result) {
            DeleteResult.Success -> {
                LOG.info("Successfully deleted ${runtime.name} $id")
                UAPIEmptyResponse
            }
            DeleteResult.AlreadyDeleted -> {
                LOG.info("${runtime.name} $id has already been deleted; returning success")
                UAPIEmptyResponse
            }
            DeleteResult.Unauthorized -> {
                LOG.warn("Unauthorized request to delete ${runtime.name} $id! User context was $userContext")
                UAPINotAuthorizedError
            }
            is DeleteResult.CannotBeDeleted -> {
                LOG.warn("${runtime.name} $id cannot be deleted: ${result.reason}")
                GenericUAPIErrorResponse(
                    statusCode = 409,
                    message = "Conflict",
                    validationInformation = listOf(result.reason)
                )
            }
            is DeleteResult.Error -> {
                LOG.error("Unexpected error deleting ${runtime.name} $id: ${result.code} ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    statusCode = result.code,
                    message = "Error",
                    validationInformation = result.errors
                )
            }
        }
    }

    private fun doDelete(
        userContext: UserContext,
        id: Id,
        model: Model?
    ): DeleteResult {
        if (model == null) {
            return DeleteResult.AlreadyDeleted
        }
        if (!operation.canUserDelete(userContext, id, model)) {
            return DeleteResult.Unauthorized
        }
        if (!operation.canBeDeleted(id, model)) {
            return DeleteResult.CannotBeDeleted("Cannot be deleted")
        }
        return operation.handleDelete(userContext, id, model)
    }
}
