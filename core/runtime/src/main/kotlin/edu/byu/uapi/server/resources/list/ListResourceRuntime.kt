package edu.byu.uapi.server.resources.list

import edu.byu.uapi.server.resources.ResourceRequestContext
import edu.byu.uapi.server.subresources.*
import edu.byu.uapi.server.types.IdentifiedModel
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.SpecConstants.FieldSets.DEFAULT_FIELDSETS
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.requests.FieldsetRequest
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.validation.ValidationEngine
import edu.byu.uapi.utility.collections.ifNotEmpty
import java.util.*

sealed class ResourceRuntime<UserContext : Any, ModelStyle : ModelHolder> : SubresourceParent<UserContext, ModelStyle> {
}

class ListResourceRuntime<UserContext : Any, Id : Any, Model : Any, Params : ListParams>(
    internal val resource: ListResource<UserContext, Id, Model, Params>,
    val typeDictionary: TypeDictionary,
    val validationEngine: ValidationEngine,
    subresourceList: List<Subresource<UserContext, IdentifiedModel<Id, Model>, *>> = emptyList()
) : ResourceRuntime<UserContext, IdentifiedModel<Id, Model>>() {

    val pluralName = resource.pluralName
    val singleName = resource.singleName

    // TODO fun validateResource(validation: Validating)

    companion object {
        private val LOG = loggerFor<ListResourceRuntime<*, *, *, *>>()
    }

    val idReader: IdParamReader<Id> = resource.getIdReader(typeDictionary)

    init {
        LOG.debug("Initializing $pluralName runtime")
    }

    val model: ListResourceModel by lazy {
        TODO()
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

    val subresources: Map<String, SubresourceRuntime<UserContext, IdentifiedModel<Id, Model>, *>> = subresourceList.map { it.createRuntime(this, typeDictionary, validationEngine) }.associateBy { it.fieldsetName }
    val availableFieldsets = DEFAULT_FIELDSETS + subresources.keys
    val availableContexts = emptyMap<String, Set<String>>()

    override fun getRequestedFieldsets(fieldsetRequest: FieldsetRequest?): RequestedFieldsetResponse {
        if (fieldsetRequest == null) {
            return RequestedFieldsetResponse.of(DEFAULT_FIELDSETS)
        }
        val requestedFieldsets = fieldsetRequest.requestedFieldsets
        val requestedContexts = fieldsetRequest.requestedContexts

        // Validate incoming fieldsets and contexts
        (requestedFieldsets - availableFieldsets).ifNotEmpty { return RequestedFieldsetResponse.InvalidFieldsets }
        (requestedContexts - availableContexts.keys).ifNotEmpty { return RequestedFieldsetResponse.InvalidContexts }

        val fromContexts = fieldsetRequest.requestedContexts.flatMap { availableContexts.getValue(it) /* we already validated contexts */ }
        val set = fieldsetRequest.requestedFieldsets.ifEmpty { DEFAULT_FIELDSETS } + fromContexts
        return RequestedFieldsetResponse.of(set)
    }

    override fun getParentModel(
        requestContext: SubresourceRequestContext,
        user: UserContext,
        idParams: IdParams
    ): ParentResult<IdentifiedModel<Id, Model>> {
        val context = ResourceRequestContext.Simple(requestContext.allRequestedSubresources, requestContext.attributes)
        val id = idReader.read(idParams)
        val model = resource.loadModel(context, user, id) ?: return ParentResult.DoesNotExist
        val authorized = resource.canUserViewModel(context, user, id, model)
        if (!authorized) return ParentResult.NotAuthorized
        return ParentResult.Success(IdentifiedModel.Simple(id, model))
    }
}