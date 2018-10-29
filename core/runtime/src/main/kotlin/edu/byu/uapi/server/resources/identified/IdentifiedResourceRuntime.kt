package edu.byu.uapi.server.resources.identified

import edu.byu.uapi.server.FIELDSET_BASIC
import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.server.schemas.*
import edu.byu.uapi.server.spi.UAPITypeError
import edu.byu.uapi.server.spi.asError
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.functional.resolve
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

    @Throws(UAPITypeError::class)
    fun constructId(params: Map<String, String>): Id {
        val deser = resource.getIdDeserializer(typeDictionary)
        return deser.read(params).resolve({it}, {throw it.asError()})
    }

    init {
        LOG.debug("Initializing runtime")
    }

    val model: IdentifiedResourceModel by lazy {
        introspect(this)
    }

    val availableOperations: Set<IdentifiedResourceOperation> by lazy {
        val ops = EnumSet.of(IdentifiedResourceOperation.FETCH)

        if (resource.createOperation != null) {
            ops.add(IdentifiedResourceOperation.CREATE)
        }
        if (resource.updateOperation != null) {
            ops.add(IdentifiedResourceOperation.UPDATE)
        }
        if (resource.deleteOperation != null) {
            ops.add(IdentifiedResourceOperation.DELETE)
        }
        if (resource.listView != null) {
            ops.add(IdentifiedResourceOperation.LIST)
        }

        Collections.unmodifiableSet(ops)
    }

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

    fun <Input : Any> handleCreate(
        userContext: UserContext,
        input: Input
    ): UAPIResponse<*> {
        val op = this.resource.createOperation ?: return UAPIOperationNotImplementedError
        if (!op.createInput.isInstance(input)) {
            throw IllegalStateException("Illegal input type in resource ${this.name}: expected ${op.createInput}, got ${input::class}")
        }
        @Suppress("UNCHECKED_CAST")
        op as IdentifiedResource.Creatable<UserContext, Id, Model, Input>
        if (!op.canUserCreate(userContext)) {
            return UAPINotAuthorizedError
        }
        // TODO: validation
        val createdId = op.handleCreate(userContext, input)

        return idToBasic(userContext, createdId, ValidationResponse(201))
    }

    fun handleFetch(
        userContext: UserContext,
        id: Id,
        requestedFieldsets: Set<String> = setOf(FIELDSET_BASIC),
        requestedContexts: Set<String> = emptySet()
    ): UAPIResponse<*> {
        val model = resource.loadModel(userContext, id) ?: return UAPINotFoundError
        if (!resource.canUserViewModel(userContext, id, model)) {
            return UAPINotAuthorizedError
        }

        val loadedFieldsets = loadFieldsets(userContext, id, model, requestedFieldsets, requestedContexts)

        return UAPIFieldsetsResponse(
            fieldsets = loadedFieldsets,
            metadata = FieldsetsMetadata(
                fieldSetsReturned = loadedFieldsets.keys,
                fieldSetsAvailable = availableFieldsets
            )
        )
    }

    private fun loadFieldsets(
        userContext: UserContext,
        id: Id,
        model: Model,
        requestedFieldsets: Set<String>,
        requestedContexts: Set<String>
    ): Map<String, UAPIResponse<*>> {
        //TODO(Return fieldsets other than basic)
        return mapOf(FIELDSET_BASIC to modelToBasic(userContext, id, model))
    }

    val availableFieldsets = setOf(FIELDSET_BASIC)
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
