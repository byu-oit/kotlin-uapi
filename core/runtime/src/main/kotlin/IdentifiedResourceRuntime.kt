package edu.byu.uapi.server

import edu.byu.uapi.server.resources.identified.*
import edu.byu.uapi.server.schemas.*
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.loggerFor
import java.time.Instant
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

class IdentifiedResourceRuntime<UserContext : Any, Id : Any, Model : Any>(
    internal val name: String,
    internal val resource: IdentifiedResource<UserContext, Id, Model>
) {

    // TODO fun validateResource(validation: Validating)

    companion object {
        private val LOG = loggerFor<IdentifiedResourceRuntime<*, *, *>>()
    }

    init {
        LOG.debug("Initializing runtime")
    }

//    val httpRoutes: Set<HttpRoute> by lazy {
//        val identifiedPath = listOf(
//            StaticPathPart(name),
//            SimplePathVariablePart("id")//TODO: Actually introspect the id type
//        )
//        val set = mutableSetOf(
//            HttpRoute(
//                identifiedPath,
//                MethodHandlers(
//                    get = FetchResourceHandler(this)
//                )
//            )
//        )
//
//        set
//    }

    val model: IdentifiedResourceModel by lazy {
        introspect(this)
    }

    enum class Operation {
        FETCH,
        CREATE,
        UPDATE,
        DELETE,
        LIST
    }

    val availableOperations: Set<Operation>

    init {
        val ops = EnumSet.of(Operation.FETCH)

        if (resource.createOperation != null) {
            ops.add(Operation.CREATE)
        }
        if (resource.updateOperation != null) {
            ops.add(Operation.UPDATE)
        }
        if (resource.deleteOperation != null) {
            ops.add(Operation.DELETE)
        }
        if (resource.listView != null || resource.pagedListView != null) {
            ops.add(Operation.LIST)
        }

        availableOperations = Collections.unmodifiableSet(ops)
    }

    internal fun idToBasic(
        userContext: UserContext,
        id: Id
    ): UAPIPropertiesResponse<Model> {
        val model = resource.loadModel(userContext, id) ?: throw IllegalStateException() //TODO: Prettier error message
        return modelToBasic(userContext, id, model)
    }

    internal fun modelToBasic(
        userContext: UserContext,
        id: Id,
        model: Model
    ): UAPIPropertiesResponse<Model> {
        return UAPIPropertiesResponse(
            metadata = UAPIResourceMeta(),
            links = generateLinks(userContext, id, model),
            properties = model
        )
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

//    fun <Input : Any> handleCreateRequest(
//        request: CreateResourceRequest<UserContext, Input>
//    ): UAPIResponse<*> {
//        @Suppress("UNCHECKED_CAST")
//        val op: CreateOperation<UserContext, Id, Input> = operations.create as CreateOperation<UserContext, Id, Input>?
//            ?: throw UnsupportedOperationException("create requests are not implemented")
//
//        return op.handleRequest(request, this.operations.read)
//    }
//
//    fun <Input : Any> handleUpdateRequest(
//        request: UpdateResourceRequest<UserContext, Id, Input>
//    ): UAPIResponse<*> {
//        @Suppress("UNCHECKED_CAST")
//        val op: UpdateOperation<UserContext, Id, Model, Input, *> = operations.update as UpdateOperation<UserContext, Id, Model, Input, *>?
//            ?: throw UnsupportedOperationException("create requests are not implemented")
//
//        return op.handleRequest(request, this.operations.read)
//    }
//
//    fun handleDeleteRequest(
//        request: DeleteResourceRequest<UserContext, Id>
//    ): UAPIResponse<*> {
//        @Suppress("UNCHECKED_CAST")
//        val op: DeleteOperation<UserContext, Id, Model> = operations.delete
//            ?: throw UnsupportedOperationException("create requests are not implemented")
//
//        return op.handleRequest(request, this.operations.read)
//    }


}

//class IdentifiedResourceListHandler<UserContext : Any, Id : Any, Model : Any, Filters: Any>(
//    private val listable: Listable<UserContext, Id, Model, Filters>
//) {
//    fun handle(request: ListResourceRequest<UserContext, Filters>): UAPIResponse<*> {
//        val list = listable.list(request.userContext, request.filters)
//
//    }
//}

inline fun <reified T> Any.takeIfType(): T? {
    return if (this is T) {
        this
    } else {
        null
    }
}

interface IdentifiedResourceOperation<UserContext : Any, Id : Any, Model : Any, RequestType : ResourceRequest<UserContext>> {
    val runtime: IdentifiedResourceRuntime<UserContext, Id, Model>
    fun handle(request: RequestType): UAPIResponse<*>
}

class IdentifiedResourceFetchHandler<UserContext : Any, Id : Any, Model : Any>(
    override val runtime: IdentifiedResourceRuntime<UserContext, Id, Model>
) : IdentifiedResourceOperation<UserContext, Id, Model, FetchResourceRequest<UserContext, Id>> {
    private val resource = runtime.resource
    override fun handle(request: FetchResourceRequest<UserContext, Id>): UAPIResponse<*> {
        val user = request.userContext
        val id = request.id
        val model = resource.loadModel(user, id) ?: return UAPINotFoundError
        if (!resource.canUserViewModel(user, id, model)) {
            return UAPINotAuthorizedError
        }
        return runtime.modelToBasic(user, id, model)
    }
}

class IdentifiedResourceCreateHandler<UserContext : Any, Id : Any, Model : Any, Input : Any>(
    override val runtime: IdentifiedResourceRuntime<UserContext, Id, Model>,
    private val create: IdentifiedResource.Creatable<UserContext, Id, Model, Input>
) : IdentifiedResourceOperation<UserContext, Id, Model, CreateResourceRequest<UserContext, Input>> {

    override fun handle(request: CreateResourceRequest<UserContext, Input>): UAPIResponse<*> {
        val user = request.userContext
        val input = request.input

        if (!create.canUserCreate(user)) {
            return UAPINotAuthorizedError
        }

        //TODO create.validateCreateInput(user, input, validation)

        val id = create.handleCreate(user, input)

        return runtime.idToBasic(user, id)
    }
}


private fun introspect(runtime: IdentifiedResourceRuntime<*, *, *>): IdentifiedResourceModel {
    val resource = runtime.resource
    val name = runtime.name

    return IdentifiedResourceModel(
        name = name,
        identifier = introspectIdentifier(name, resource.idType),
        responseModel = introspectResponseModel(resource.responseFields),
        listViewModel = introspectListView(resource.listView, resource.pagedListView),
        mutations = IdentifiedResourceMutations(
            introspect(resource.createOperation),
            introspect(resource.updateOperation, resource.createWithIdOperation),
            introspect(resource.deleteOperation)
        )
    )
}

fun introspect(deleteOperation: IdentifiedResource.Deletable<*, *, *>?): DeleteOperationModel? {
    TODO("not implemented")
}

fun introspect(
    updateOperation: IdentifiedResource.Updatable<*, *, *, *>?,
    createWithIdOperation: IdentifiedResource.CreatableWithId<*, *, *, *>?
): UpdateOperationModel? {
    TODO()
}

fun introspect(runtime: IdentifiedResource.Creatable<*, *, *, *>?): CreateOperationModel? {
    TODO()
}

fun introspectListView(
    listView: IdentifiedResource.Listable<*, *, *, *>?,
    pagedListView: IdentifiedResource.PagedListable<*, *, *, *>?
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
