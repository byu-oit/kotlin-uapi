package edu.byu.uapi.server.resources.identified

import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.server.response.UAPIResponseInit
import edu.byu.uapi.server.response.uapiResponse
import edu.byu.uapi.server.spi.asError
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.functional.resolve
import edu.byu.uapi.spi.input.*
import edu.byu.uapi.spi.validation.Validating
import kotlin.reflect.KClass

interface IdentifiedResource<UserContext : Any, Id : Any, Model : Any> {

    val idType: KClass<Id>

    fun loadModel(
        userContext: UserContext,
        id: Id
    ): Model?

    fun canUserViewModel(
        userContext: UserContext,
        id: Id,
        model: Model
    ): Boolean

    fun idFromModel(model: Model): Id

    fun getIdDeserializer(context: TypeDictionary): PathParamReader<Id> {
        return context.pathDeserializer(idType).resolve({ it }, { throw it.asError() })
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

    interface Creatable<UserContext : Any, Id : Any, Model : Any, Input : Any> {
        fun canUserCreate(userContext: UserContext): Boolean
        fun validateCreateInput(
            userContext: UserContext,
            input: Input,
            validation: Validating
        )

        fun handleCreate(
            userContext: UserContext,
            input: Input
        ): Id

        val createInput: KClass<Input>
    }

    interface Deletable<UserContext : Any, Id : Any, Model : Any> {
        fun canUserDelete(
            userContext: UserContext,
            id: Id,
            model: Model
        ): Boolean

        fun canBeDeleted(
            id: Id,
            model: Model
        ): Boolean

        fun handleDelete(
            userContext: UserContext,
            id: Id,
            model: Model
        )
    }

    interface Listable<UserContext : Any, Id : Any, Model : Any, CollectionParams : ListParams> {
        fun list(
            userContext: UserContext,
            params: CollectionParams
        ): List<Model>

        val listParamsType: KClass<CollectionParams>

        fun getListParamReader(dictionary: TypeDictionary): MaybeTypeFailure<ListParamReader<CollectionParams>> {
            return dictionary.listParamReader(this.listParamsType)
        }

        interface NoParams<UserContext: Any, Id: Any, Model: Any>: Listable<UserContext, Id, Model, ListParams.Empty> {
            override val listParamsType: KClass<ListParams.Empty>
                get() = ListParams.Empty::class
        }

        interface WithSubset<UserContext : Any, Id : Any, Model : Any, CollectionParams : ListParams.SubSetting> :
            Listable<UserContext, Id, Model, CollectionParams> {

            override fun list(
                userContext: UserContext,
                params: CollectionParams
            ): ListWithTotal<Model>

            val listDefaultSubsetSize: Int
            val listMaxSubsetSize: Int
        }

        interface WithSearch<UserContext : Any, Id : Any, Model : Any, CollectionParams : ListParams.Searching<SearchContext>, SearchContext : Enum<SearchContext>>
            : Listable<UserContext, Id, Model, CollectionParams> {
            fun listSearchContexts(value: SearchContext): Collection<String>
        }

        interface WithSort<UserContext : Any, Id : Any, Model : Any, CollectionParams : ListParams.Sorting<SortField>, SortField : Enum<SortField>>
            : Listable<UserContext, Id, Model, CollectionParams> {
            val listDefaultSortFields: List<SortField>
            val listDefaultSortOrder: SortOrder
        }

        interface WithFilters<UserContext : Any, Id : Any, Model : Any, CollectionParams : ListParams.Filtering<Filters>, Filters : Any>
            : Listable<UserContext, Id, Model, CollectionParams> {

        }
    }

    interface Updatable<UserContext : Any, Id : Any, Model : Any, Input : Any> {
        fun canUserUpdate(
            userContext: UserContext,
            id: Id,
            model: Model
        ): Boolean

        fun canBeUpdated(
            id: Id,
            model: Model
        ): Boolean

        fun validateUpdateInput(
            userContext: UserContext,
            id: Id,
            model: Model,
            input: Input,
            validation: Validating
        )

        fun handleUpdate(
            userContext: UserContext,
            id: Id,
            model: Model,
            input: Input
        )

        val updateInput: KClass<Input>
    }

    interface UpdatableOrCreatable<UserContext : Any, Id : Any, Model : Any, Input : Any> : Updatable<UserContext, Id, Model, Input> {
        fun canUserCreateWithId(
            userContext: UserContext,
            id: Id
        ): Boolean

        fun validateCreateWithIdInput(
            userContext: UserContext,
            id: Id,
            input: Input,
            validation: Validating
        )

        fun handleCreateWithId(
            userContext: UserContext,
            input: Input,
            id: Id
        )
    }
}

inline fun <UserContext : Any, Model : Any> IdentifiedResource<UserContext, *, Model>.fields(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
    : List<ResponseField<UserContext, Model, *>> = uapiResponse(fn)
