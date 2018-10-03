package edu.byu.uapi.server.resources.identified

import edu.byu.uapi.server.inputs.TypeDictionary
import edu.byu.uapi.server.inputs.PathParamDeserializer
import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.server.validation.Validating
import kotlin.reflect.KClass

interface IdentifiedResource<UserContext : Any, Id : Any, Model : Any> {

    val idType: KClass<Id>

    fun loadModel(userContext: UserContext, id: Id): Model?
    fun canUserViewModel(userContext: UserContext, id: Id, model: Model): Boolean
    fun idFromModel(model: Model): Id

    fun getIdDeserializer(context: TypeDictionary): PathParamDeserializer<Id> {
        return context.pathDeserializer(idType).resolve({it}, { throw it.asError() })
    }

    // TODO: Maybe it would be better to have a map of types to field definitions/renderers? That'll especially help with trees of objects.
    //   Then again, it's not THAT hard to share around a list representing the fields for a given type...
    val responseFields: List<ResponseField<UserContext, Model, *>>

    val createOperation: Creatable<UserContext, Id, Model, *>?
        get() = this.takeIfType()
    val updateOperation: Updatable<UserContext, Id, Model, *>?
        get() = this.takeIfType()
    val deleteOperation: Deletable<UserContext, Id, Model>?
        get() = this.takeIfType()

    val listView: Listable<UserContext, Id, Model, *>?
        get() = this.takeIfType()

    val pagedListView: PagedListable<UserContext, Id, Model, *>?
        get() = this.takeIfType()

    interface Creatable<UserContext : Any, Id : Any, Model : Any, Input : Any> {
        fun canUserCreate(userContext: UserContext): Boolean
        fun validateCreateInput(userContext: UserContext, input: Input, validation: Validating)
        fun handleCreate(userContext: UserContext, input: Input): Id

        val createInput: KClass<Input>
    }

    interface Deletable<UserContext : Any, Id : Any, Model : Any> {
        fun canUserDelete(userContext: UserContext, id: Id, model: Model): Boolean
        fun canBeDeleted(id: Id, model: Model): Boolean
        fun handleDelete(userContext: UserContext, id: Id, model: Model)
    }

    interface Listable<UserContext : Any, Id : Any, Model : Any, CollectionParams : Any> {
        fun list(userContext: UserContext, params: CollectionParams): Collection<Model>

        val paramsType: KClass<CollectionParams>
    }

//    interface ListableById<UserContext : Any, Id : Any, Model : Any, Filters : Any>: Listable<UserContext, Id, Model, Filters>{
//
//        val runtime: IdentifiedResource<UserContext, Id, Model>
//
//        fun listIds(userContext: UserContext, filters: Filters): Collection<Id>
//
//        override fun list(userContext: UserContext, filters: Filters): Collection<Model> {
//            return listIds(userContext, filters).map { runtime.loadModel(userContext, it)!! }
//        }
//    }

    interface PagedListable<UserContext : Any, Id : Any, Model : Any, CollectionParams : Any> {
        fun list(userContext: UserContext, filters: CollectionParams, paging: PagingParams): CollectionWithTotal<Model>

        val paramsType: KClass<CollectionParams>
        val defaultPageSize: Int
        val maxPageSize: Int
    }

//    interface PagedListableById<UserContext : Any, Id : Any, Model : Any, Filters : Any>: PagedListable<UserContext, Id, Model, Filters> {
//
//        val runtime: IdentifiedResource<UserContext, Id, Model>
//
//        fun listIds(userContext: UserContext, filters: Filters, paging: PagingParams): CollectionWithTotal<Id>
//
//        override fun list(userContext: UserContext, filters: Filters, paging: PagingParams): CollectionWithTotal<Model> {
//            val ids = listIds(userContext, filters, paging)
//            return CollectionWithTotal(ids.totalItems, ids.map { runtime.loadModel(userContext, it)!! })
//        }
//    }

    interface Updatable<UserContext : Any, Id : Any, Model : Any, Input : Any> {
        fun canUserUpdate(userContext: UserContext, id: Id, model: Model): Boolean
        fun canBeUpdated(id: Id, model: Model): Boolean
        fun validateUpdateInput(userContext: UserContext, id: Id, model: Model, input: Input, validation: Validating)
        fun handleUpdate(userContext: UserContext, id: Id, model: Model, input: Input)

        val updateInput: KClass<Input>
    }

    interface UpdatableOrCreatable<UserContext : Any, Id : Any, Model : Any, Input : Any>: Updatable<UserContext, Id, Model, Input> {
        fun canUserCreateWithId(userContext: UserContext, id: Id): Boolean
        fun validateCreateWithIdInput(userContext: UserContext, id: Id, input: Input, validation: Validating)
        fun handleCreateWithId(userContext: UserContext, input: Input, id: Id)
    }
}

data class CollectionWithTotal<T>(
    val totalItems: Int,
    private val values: Collection<T>
) : Collection<T> by values

data class PagingParams(
    val pageStart: Int,
    val pageSize: Int
)

