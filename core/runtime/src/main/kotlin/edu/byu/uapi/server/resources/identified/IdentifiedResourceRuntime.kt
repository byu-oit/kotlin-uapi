package edu.byu.uapi.server.resources.identified

import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.server.schemas.*
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.SpecConstants
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ListParamReader
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.input.ListWithTotal
import edu.byu.uapi.spi.requests.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class IdentifiedResourceRuntime<UserContext : Any, Id : Any, Model : Any>(
    val name: String,
    internal val resource: IdentifiedResource<UserContext, Id, Model>,
    val typeDictionary: TypeDictionary
) {

    // TODO fun validateResource(validation: Validating)

    companion object {
        private val LOG = loggerFor<IdentifiedResourceRuntime<*, *, *>>()
    }

    val idReader: IdParamReader<Id> = resource.getIdReader(typeDictionary, this.name + "_")

    init {
        LOG.debug("Initializing runtime")
    }

    val model: IdentifiedResourceModel by lazy {
        introspect(this)
    }

    val availableOperations: Set<IdentifiedResourceRequestHandler<UserContext, Id, Model, *>> by lazy {
        val ops: MutableSet<IdentifiedResourceRequestHandler<UserContext, Id, Model, *>> = mutableSetOf(IdentifiedResourceFetchHandler(this))
        resource.listView?.also {
            ops.add(IdentifiedResourceListHandler(this, it))
        }

        Collections.unmodifiableSet(ops)
    }

//    fun <Input : Any> handleCreate(
//        userContext: UserContext,
//        input: Input
//    ): UAPIResponse<*> {
//        val op = this.resource.createOperation ?: return UAPIOperationNotImplementedError
//        if (!op.createInput.isInstance(input)) {
//            throw IllegalStateException("Illegal input type in resource ${this.name}: expected ${op.createInput}, got ${input::class}")
//        }
//        @Suppress("UNCHECKED_CAST")
//        op as IdentifiedResource.Creatable<UserContext, Id, Model, Input>
//        if (!op.canUserCreate(userContext)) {
//            return UAPINotAuthorizedError
//        }
//        // TODO: validation
//        val createdId = op.handleCreate(userContext, input)
//
//        return idToBasic(userContext, createdId, ValidationResponse(201))
//    }


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

private fun introspect(runtime: IdentifiedResourceRuntime<*, *, *>): IdentifiedResourceModel {
    val resource = runtime.resource
    val name = runtime.name

    return IdentifiedResourceModel(
        name = name,
        identifier = introspectIdentifier(name, resource.idType),
        responseModel = introspectResponseModel(resource.responseFields),
        listViewModel = introspectListView(resource.listView),
        mutations = IdentifiedResourceMutations(
            introspect(resource.createOperation),
            introspect(resource.updateOperation),
            introspect(resource.deleteOperation)
        )
    )
}

fun introspect(deleteOperation: IdentifiedResource.Deletable<*, *, *>?): DeleteOperationModel? {
    TODO("not implemented")
}

fun introspect(
    updateOperation: IdentifiedResource.Updatable<*, *, *, *>?
): UpdateOperationModel? {
    TODO()
}

fun introspect(runtime: IdentifiedResource.Creatable<*, *, *, *>?): CreateOperationModel? {
    TODO()
}

fun introspectListView(
    listView: IdentifiedResource.Listable<*, *, *, *>?
): ListViewModel? {
    TODO("not implemented")
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

sealed class IdentifiedResourceRequestHandler<UserContext : Any, Id : Any, Model : Any, Request : IdentifiedResourceRequest<UserContext>>(
    val runtime: IdentifiedResourceRuntime<UserContext, Id, Model>
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

class IdentifiedResourceFetchHandler<UserContext : Any, Id : Any, Model : Any>(
    runtime: IdentifiedResourceRuntime<UserContext, Id, Model>
) : IdentifiedResourceRequestHandler<UserContext, Id, Model, FetchIdentifiedResource<UserContext>>(runtime) {
    override fun handle(
        request: FetchIdentifiedResource<UserContext>
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

class IdentifiedResourceListHandler<UserContext : Any, Id : Any, Model : Any, Params : ListParams>(
    runtime: IdentifiedResourceRuntime<UserContext, Id, Model>,
    private val listView: IdentifiedResource.Listable<UserContext, Id, Model, Params>
) : IdentifiedResourceRequestHandler<UserContext, Id, Model, ListIdentifiedResource<UserContext>>(runtime) {
    private val paramReader: ListParamReader<Params> = listView.getListParamReader(runtime.typeDictionary)

    override fun handle(request: ListIdentifiedResource<UserContext>): UAPIResponse<*> {
        val params = paramReader.read(request.queryParams)

        val result = listView.list(request.userContext, params)

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

class IdentifiedResourceCreateHandler<UserContext : Any, Id : Any, Model : Any, Input : Any>(
    runtime: IdentifiedResourceRuntime<UserContext, Id, Model>,
    private val createOperation: IdentifiedResource.Creatable<UserContext, Id, Model, Input>
) : IdentifiedResourceRequestHandler<UserContext, Id, Model, CreateIdentifiedResource<UserContext>>(runtime) {
    override fun handle(request: CreateIdentifiedResource<UserContext>): UAPIResponse<*> {
        val userContext = request.userContext

        val authorized = createOperation.canUserCreate(userContext)
        if (!authorized) {
            return UAPINotAuthorizedError
        }
        val input = request.body.readAs(createOperation.createInput)
        // TODO: validation
        val createdId = createOperation.handleCreate(userContext, input)

        return super.idToBasic(
            userContext = userContext,
            id = createdId,
            validationResponse = ValidationResponse(code = 201, message = "Created")
        )
    }
}
