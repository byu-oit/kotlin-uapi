package edu.byu.uapi.server.subresources.list

import edu.byu.uapi.model.UAPISortOrder
import edu.byu.uapi.server.inputs.create
import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.server.response.UAPIResponseInit
import edu.byu.uapi.server.response.uapiResponse
import edu.byu.uapi.server.scalars.EnumScalarType
import edu.byu.uapi.server.spi.*
import edu.byu.uapi.server.spi.reflective.ReflectiveFilterParamReader
import edu.byu.uapi.server.spi.reflective.ReflectiveIdParamReader
import edu.byu.uapi.server.subresources.Subresource
import edu.byu.uapi.server.subresources.SubresourceRequestContext
import edu.byu.uapi.server.types.CreateResult
import edu.byu.uapi.server.types.DeleteResult
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.server.types.UpdateResult
import edu.byu.uapi.server.util.extrapolateGenericType
import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.*
import edu.byu.uapi.spi.validation.ValidationEngine
import edu.byu.uapi.spi.validation.Validator
import edu.byu.uapi.utility.takeIfType
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

interface ListSubresource<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Params : ListParams>
    : Subresource<UserContext, Parent, Model> {

    val pluralName: String

    val singleName: String
        get() = defaultSingleName()

    val idType: KClass<Id>
        get() = extrapolateGenericType("Id", ListSubresource<*, *, *, *, *>::idType)

    val scalarIdParamName: String
        get() = this.singleName + "_id"

    fun loadModel(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        id: Id
    ): Model?

    fun canUserViewModel(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        id: Id,
        model: Model
    ): Boolean

    fun idFromModel(model: Model): Id

    @Throws(UAPITypeError::class)
    fun getIdReader(
        dictionary: TypeDictionary
    ): IdParamReader<Id> {
        return defaultIdReader(dictionary)
    }

    fun list(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        params: Params
    ): List<Model>

    val listParamsType: KClass<Params>
        get() = extrapolateGenericType("Params", ListSubresource<*, *, *, *, *>::listParamsType)

    @Throws(UAPITypeError::class)
    fun getListParamReader(dictionary: TypeDictionary): ListParamReader<Params> {
        return defaultGetListParamReader(dictionary)
    }

    // TODO: Maybe it would be better to have a map of types to properties definitions/renderers? That'll especially help with trees of objects.
    //   Then again, it's not THAT hard to share around a list representing the properties for a given type...
    val responseFields: List<ResponseField<UserContext, Model, *>>

    val createOperation: Creatable<UserContext, Parent, Id, Model, *>?
        get() = this.takeIfType()

    val updateOperation: Updatable<UserContext, Parent, Id, Model, *>?
        get() = this.takeIfType()

    val deleteOperation: Deletable<UserContext, Parent, Id, Model>?
        get() = this.takeIfType()

    interface Creatable<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Input : Any> {
        fun canUserCreate(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent
        ): Boolean

        fun getCreateValidator(validationEngine: ValidationEngine): Validator<Input> {
            return validationEngine.validatorFor(createInput)
        }

        fun handleCreate(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            input: Input
        ): CreateResult<Model>

        val createInput: KClass<Input>
            get() = extrapolateGenericType("Input", Creatable<*, *, *, *, *>::createInput)
    }

    interface Deletable<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any> {
        fun canUserDelete(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            id: Id,
            model: Model
        ): Boolean

        fun canBeDeleted(
            parent: Parent,
            id: Id,
            model: Model
        ): Boolean

        fun handleDelete(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            id: Id,
            model: Model
        ): DeleteResult
    }

    interface Updatable<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Input : Any> {
        fun canUserUpdate(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            id: Id,
            model: Model
        ): Boolean

        fun canBeUpdated(
            parent: Parent,
            id: Id,
            model: Model
        ): Boolean

        fun getUpdateValidator(validationEngine: ValidationEngine): Validator<Input> {
            return validationEngine.validatorFor(updateInput)
        }

        fun handleUpdate(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            id: Id,
            model: Model,
            input: Input
        ): UpdateResult<Model>

        val updateInput: KClass<Input>
            get() = extrapolateGenericType("Input", Updatable<*, *, *, *, *>::updateInput)
    }

    interface CreatableWithId<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Input : Any> : Updatable<UserContext, Parent, Id, Model, Input> {
        fun canUserCreateWithId(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            id: Id
        ): Boolean

        fun handleCreateWithId(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            id: Id,
            input: Input
        ): CreateResult<Model>
    }

    interface Simple<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any> : ListSubresource<UserContext, Parent, Id, Model, ListParams.Empty> {
        override val listParamsType: KClass<ListParams.Empty>
            get() = ListParams.Empty::class

        override fun getListParamReader(dictionary: TypeDictionary): ListParamReader<ListParams.Empty> {
            return EmptyListParamReader
        }
    }

    interface ListWithSubset<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, CollectionParams : ListParams.WithSubset> :
        ListSubresource<UserContext, Parent, Id, Model, CollectionParams> {

        override fun list(
            requestContext: SubresourceRequestContext,
            userContext: UserContext,
            parent: Parent,
            params: CollectionParams
        ): ListWithTotal<Model>

        val listDefaultSubsetSize: Int
        val listMaxSubsetSize: Int
    }

    interface ListWithSearch<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, CollectionParams : ListParams.WithSearch<SearchContext>, SearchContext : Enum<SearchContext>>
        : ListSubresource<UserContext, Parent, Id, Model, CollectionParams> {

        fun listSearchContexts(value: SearchContext): Collection<String>

        @Throws(UAPITypeError::class)
        fun getListSearchParamReader(dictionary: TypeDictionary): SearchParamsReader<SearchContext> {
            return defaultListSearchParamReader(dictionary)
        }

        @Throws(UAPITypeError::class)
        fun getListSearchContextType(dictionary: TypeDictionary): EnumScalarType<SearchContext> {
            return DefaultParameterStyleEnumScalar(
                extrapolateGenericType("SearchContext", ListWithSearch<*, *, *, *, *, *>::getListSearchContextType)
            )
        }
    }

    interface ListWithSort<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, CollectionParams : ListParams.WithSort<SortProperty>, SortProperty : Enum<SortProperty>>
        : ListSubresource<UserContext, Parent, Id, Model, CollectionParams> {

        val listDefaultSortProperties: List<SortProperty>
        val listDefaultSortOrder: UAPISortOrder

        @Throws(UAPITypeError::class)
        fun getListSortPropertyType(typeDictionary: TypeDictionary): EnumScalarType<SortProperty> {
            return DefaultParameterStyleEnumScalar(
                extrapolateGenericType("SortProperty", ListWithSort<*, *, *, *, *, *>::getListSortPropertyType)
            )
        }

        @Throws(UAPITypeError::class)
        fun getListSortParamsReader(typeDictionary: TypeDictionary): SortParamsReader<SortProperty> {
            return defaultListSortParamsReader(typeDictionary)
        }
    }

    interface ListWithFilters<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, CollectionParams : ListParams.WithFilters<Filters>, Filters : Any>
        : ListSubresource<UserContext, Parent, Id, Model, CollectionParams> {


        val listFilterParamType: KClass<Filters>
            get() = this.extrapolateGenericType("Filters", ListWithFilters<*, *, *, *, *, *>::listFilterParamType)

        @Throws(UAPITypeError::class)
        fun getListFilterParamReader(typeDictionary: TypeDictionary): FilterParamsReader<Filters> {
            return ReflectiveFilterParamReader.create(listFilterParamType, typeDictionary)
        }
    }
}

private fun ListSubresource<*, *, *, *, *>.defaultSingleName(): String {
    if (pluralName.endsWith("s")) {
        return pluralName.dropLast(1)
    }
    return pluralName
}

private fun <Id : Any, Model : Any, UserContext : Any> ListSubresource<UserContext, *, Id, Model, *>.defaultIdReader(
    dictionary: TypeDictionary
): IdParamReader<Id> {
    val idType = this.idType
    val prefix = this.singleName + "_"
    if (dictionary.isScalarType(idType)) {
        return ScalarTypeIdParamReader(this.scalarIdParamName, dictionary.requireScalarType(idType))
    }
    return ReflectiveIdParamReader.create(prefix, idType, dictionary)
}

@Throws(UAPITypeError::class)
internal fun <Params : ListParams>
    ListSubresource<*, *, *, *, Params>.defaultGetListParamReader(
    dictionary: TypeDictionary
): ListParamReader<Params> {
    if (listParamsType == ListParams.Empty::class) {
        @Suppress("UNCHECKED_CAST")
        return EmptyListParamReader as ListParamReader<Params>
    }
    if (!listParamsType.isData) {
        throw UAPITypeError.create(listParamsType, "List parameter type must be a data class.")
    }
    val ctor = listParamsType.primaryConstructor
        ?: throw UAPITypeError.create(listParamsType, "List parameter type must have a primary constructor.")

    val search = this.takeIfType<ListSubresource.ListWithSearch<*, *, *, *, *, *>>()?.getListSearchParamReader(dictionary)
    val filter = this.takeIfType<ListSubresource.ListWithFilters<*, *, *, *, *, *>>()?.getListFilterParamReader(dictionary)
    val sort = this.takeIfType<ListSubresource.ListWithSort<*, *, *, *, *, *>>()?.getListSortParamsReader(dictionary)
    val subset = this.takeIfType<ListSubresource.ListWithSubset<*, *, *, *, *>>()?.let { it ->
        SubsetParamsReader(
            defaultSize = it.listDefaultSubsetSize,
            maxSize = it.listMaxSubsetSize
        )
    }

    return DefaultListParamReader.create(
        paramsType = listParamsType,
        search = search,
        filter = filter,
        sort = sort,
        subset = subset,
        constructor = ctor
    )
}

@Throws(UAPITypeError::class)
internal fun <SearchContext : Enum<SearchContext>>
    ListSubresource.ListWithSearch<*, *, *, *, *, SearchContext>.defaultListSearchParamReader(
    dictionary: TypeDictionary
): SearchParamsReader<SearchContext> {
    val contextType = getListSearchContextType(dictionary)
    val contextClass = contextType.type
    val searchContexts = contextType.enumConstants.map { it to listSearchContexts(it) }.toMap()

    if (searchContexts.any { it.value.isEmpty() }) {
        throw UAPITypeError.create(contextType.type, "${this::class.simpleName}.listSearchContexts must return a non-empty set for every value of ${contextClass.simpleName}")
    }

    return DefaultSearchParamsReader.create(
        contextType, searchContexts
    )
}

@Throws(UAPITypeError::class)
internal fun <SortProperty : Enum<SortProperty>>
    ListSubresource.ListWithSort<*, *, *, *, *, SortProperty>.defaultListSortParamsReader(
    typeDictionary: TypeDictionary
): SortParamsReader<SortProperty> {
    val sortPropType = getListSortPropertyType(typeDictionary)

    return DefaultSortParamsReader.create(
        sortPropType,
        EnumScalarType(UAPISortOrder::class),
        listDefaultSortProperties,
        listDefaultSortOrder
    )
}

inline fun <UserContext : Any, Model : Any> ListSubresource<UserContext, *, *, Model, *>.fields(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
    : List<ResponseField<UserContext, Model, *>> = uapiResponse(fn)
