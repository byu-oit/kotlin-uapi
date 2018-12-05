package edu.byu.uapi.server.resources.list

import edu.byu.uapi.server.subresources.*
import edu.byu.uapi.server.types.IdentifiedModel
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.SpecConstants
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.validation.ValidationEngine
import java.util.*

sealed class ResourceRuntime<UserContext: Any, ModelStyle: ModelHolder>: SubresourceParent<UserContext, ModelStyle> {

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
    val availableFieldsets = setOf(SpecConstants.FieldSets.VALUE_BASIC) + subresources.keys
    val availableContexts = emptyMap<String, Set<String>>()

    override fun getParentModel(
        user: UserContext,
        idParams: IdParams
    ): ParentResult<IdentifiedModel<Id, Model>> {
        val id = idReader.read(idParams)
        val model = resource.loadModel(user, id) ?: return ParentResult.DoesNotExist
        val authorized = resource.canUserViewModel(user, id, model)
        if (!authorized) return ParentResult.NotAuthorized
        return ParentResult.Success(IdentifiedModel.Simple(id, model))
    }
}
