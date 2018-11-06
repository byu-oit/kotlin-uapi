package edu.byu.uapi.server.resources.identified

import edu.byu.uapi.server.inputs.typeError
import edu.byu.uapi.server.inputs.typeFailure
import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.server.response.UAPIResponseInit
import edu.byu.uapi.server.response.uapiResponse
import edu.byu.uapi.server.scalars.EnumScalarType
import edu.byu.uapi.server.spi.*
import edu.byu.uapi.server.spi.reflective.ReflectiveFilterParamReader
import edu.byu.uapi.server.spi.reflective.ReflectiveIdParamReader
import edu.byu.uapi.server.util.DarkMagic
import edu.byu.uapi.server.util.DarkMagicException
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.functional.useFailure
import edu.byu.uapi.spi.input.*
import edu.byu.uapi.spi.validation.Validating
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

interface IdentifiedResource<UserContext : Any, Id : Any, Model : Any> {

    val idType: KClass<Id>
        get() = defaultIdType()

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

    fun getIdReader(
        dictionary: TypeDictionary,
        paramPrefix: String
    ): MaybeTypeFailure<IdParamReader<Id>> {
        return defaultIdReader(dictionary, paramPrefix)
    }

    // TODO: Maybe it would be better to have a map of types to properties definitions/renderers? That'll especially help with trees of objects.
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

    interface Listable<UserContext : Any, Id : Any, Model : Any, Params : ListParams> {
        fun list(
            userContext: UserContext,
            params: Params
        ): List<Model>

        val listParamsType: KClass<Params>

        fun getListParamReader(dictionary: TypeDictionary): MaybeTypeFailure<ListParamReader<Params>> {
            return defaultGetListParamReader(dictionary)
        }

        interface NoParams<UserContext : Any, Id : Any, Model : Any> : Listable<UserContext, Id, Model, ListParams.Empty> {
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

            fun getListSearchParamReader(dictionary: TypeDictionary): MaybeTypeFailure<SearchParamsReader<SearchContext>> {
                return defaultListSearchParamReader(dictionary)
            }

            fun getListSearchContextType(dictionary: TypeDictionary): MaybeTypeFailure<EnumScalarType<SearchContext>> {
                return defaultListSearchContextType(dictionary)
            }
        }

        interface WithSort<UserContext : Any, Id : Any, Model : Any, CollectionParams : ListParams.Sorting<SortProperty>, SortProperty : Enum<SortProperty>>
            : Listable<UserContext, Id, Model, CollectionParams> {

            val listDefaultSortProperties: List<SortProperty>
            val listDefaultSortOrder: SortOrder

            fun getListSortPropertyType(typeDictionary: TypeDictionary): MaybeTypeFailure<EnumScalarType<SortProperty>> {
                return defaultListSortPropertyType(typeDictionary)
            }

            fun getListSortParamsReader(typeDictionary: TypeDictionary): MaybeTypeFailure<SortParamsReader<SortProperty>> {
                return defaultListSortParamsReader(typeDictionary)
            }
        }

        interface WithFilters<UserContext : Any, Id : Any, Model : Any, CollectionParams : ListParams.Filtering<Filters>, Filters : Any>
            : Listable<UserContext, Id, Model, CollectionParams> {
            fun getListFilterParamReader(typeDictionary: TypeDictionary): MaybeTypeFailure<FilterParamsReader<Filters>> {
                return defaultListFilterParamReader(typeDictionary)
            }
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

private fun <Id : Any, Model : Any, UserContext : Any> IdentifiedResource<UserContext, Id, Model>.defaultIdReader(
    dictionary: TypeDictionary,
    paramPrefix: String
): MaybeTypeFailure<IdParamReader<Id>> {
    val idType = this.idType
    if (dictionary.isScalarType(idType)) {
        return Success(ScalarTypeIdParamReader(paramPrefix, dictionary.requireScalarType(idType)))
    }
    return ReflectiveIdParamReader.create(paramPrefix, idType, dictionary)
}

private fun <Id : Any, Model : Any, UserContext : Any> IdentifiedResource<UserContext, Id, Model>.defaultIdType(): KClass<Id> {
    return try {
        val supertype = DarkMagic.findMatchingSupertype(this::class, IdentifiedResource::class)
            ?: typeError(this::class, "Unable to get IdentifiedResource ID Type")
        val idProjection = supertype.arguments[1]
        DarkMagic.getConcreteType(idProjection)
    } catch (ex: DarkMagicException) {
        typeError(this::class, "Unable to get ID type", ex)
    }
}

internal fun <Params : ListParams>
    IdentifiedResource.Listable<*, *, *, Params>.defaultGetListParamReader(
    dictionary: TypeDictionary
): MaybeTypeFailure<ListParamReader<Params>> {
    if (listParamsType == ListParams.Empty::class) {
        @Suppress("UNCHECKED_CAST")
        return Success(EmptyListParamReader as ListParamReader<Params>)
    }
    if (!listParamsType.isData) {
        typeError(listParamsType, "List parameter type must be a data class.")
    }
    val ctor = listParamsType.primaryConstructor
        ?: typeError(listParamsType, "List parameter type must have a primary constructor.")

    val search = this.takeIfType<IdentifiedResource.Listable.WithSearch<*, *, *, *, *>>()?.getListSearchParamReader(dictionary)
        ?.useFailure { return it }
    val filter = this.takeIfType<IdentifiedResource.Listable.WithFilters<*, *, *, *, *>>()?.getListFilterParamReader(dictionary)
        ?.useFailure { return it }
    val sort = this.takeIfType<IdentifiedResource.Listable.WithSort<*, *, *, *, *>>()?.getListSortParamsReader(dictionary)
        ?.useFailure { return it }
    val subset = this.takeIfType<IdentifiedResource.Listable.WithSubset<*, *, *, *>>()?.let { it ->
        SubsetParamsReader(
            defaultSize = it.listDefaultSubsetSize,
            maxSize = it.listMaxSubsetSize
        )
    }

    return DefaultListParamReader.create(
        search = search,
        filter = filter,
        sort = sort,
        subset = subset,
        constructor = ctor
    )
}


internal fun <SearchContext : Enum<SearchContext>>
    IdentifiedResource.Listable.WithSearch<*, *, *, *, SearchContext>.defaultListSearchParamReader(
    dictionary: TypeDictionary
): MaybeTypeFailure<SearchParamsReader<SearchContext>> {
    val contextType = getListSearchContextType(dictionary)
        .useFailure { return it }
    val contextClass = contextType.type
    val searchContexts = contextType.enumConstants.map { it to listSearchContexts(it) }.toMap()

    if (searchContexts.any { it.value.isEmpty() }) {
        return typeFailure(contextType.type, "${this::class.simpleName}.listSearchContexts must return a non-empty set for every value of ${contextClass.simpleName}")
    }

    return DefaultSearchParamsReader.create(
        contextType, searchContexts
    )
}

internal fun <SearchContext : Enum<SearchContext>>
    IdentifiedResource.Listable.WithSearch<*, *, *, *, SearchContext>.defaultListSearchContextType(
    dictionary: TypeDictionary
): MaybeTypeFailure<EnumScalarType<SearchContext>> {
    val searchContextType: KClass<SearchContext> = try {
        val withSearch = DarkMagic.findMatchingSupertype(this::class, IdentifiedResource.Listable.WithSearch::class)
            ?: throw DarkMagicException("This shouldn't be possible! Somebody broke the compiler!")
        val projection = withSearch.arguments[4]
        DarkMagic.getConcreteType(projection)
    } catch (ex: DarkMagicException) {
        return typeFailure(this::class, "Unable to get search context type", ex)
    }
    return Success(DefaultParameterStyleEnumScalar(searchContextType))
}

internal fun <SortProperty : Enum<SortProperty>>
    IdentifiedResource.Listable.WithSort<*, *, *, *, SortProperty>.defaultListSortPropertyType(
    typeDictionary: TypeDictionary
): MaybeTypeFailure<EnumScalarType<SortProperty>> {
    val sortPropertyType: KClass<SortProperty> = try {
        val withSearch = DarkMagic.findMatchingSupertype(this::class, IdentifiedResource.Listable.WithSort::class)
            ?: throw DarkMagicException("This shouldn't be possible! Somebody broke the compiler!")
        val projection = withSearch.arguments[4]
        DarkMagic.getConcreteType(projection)
    } catch (ex: DarkMagicException) {
        return typeFailure(this::class, "Unable to get search context type", ex)
    }
    return Success(DefaultParameterStyleEnumScalar(sortPropertyType))
}

internal fun <SortProperty : Enum<SortProperty>>
    IdentifiedResource.Listable.WithSort<*, *, *, *, SortProperty>.defaultListSortParamsReader(
    typeDictionary: TypeDictionary
): MaybeTypeFailure<SortParamsReader<SortProperty>> {
    val sortPropType = getListSortPropertyType(typeDictionary)
        .useFailure { return it }

    return DefaultSortParamsReader.create(
        sortPropType,
        EnumScalarType(SortOrder::class),
        listDefaultSortProperties,
        listDefaultSortOrder
    )
}

internal fun <Filters : Any>
    IdentifiedResource.Listable.WithFilters<*, *, *, *, Filters>.defaultListFilterParamReader(
    typeDictionary: TypeDictionary
): MaybeTypeFailure<FilterParamsReader<Filters>> {
    val filterType: KClass<Filters> = try {
        val withFilters = DarkMagic.findMatchingSupertype(this::class, IdentifiedResource.Listable.WithFilters::class)
            ?: throw DarkMagicException("This shouldn't be possible! Somebody broke the compiler!")
        val projection = withFilters.arguments[4]
        DarkMagic.getConcreteType(projection)
    } catch (ex: DarkMagicException) {
        return typeFailure(this::class, "Unable to get list filter type", ex)
    }
    return ReflectiveFilterParamReader.create(filterType, typeDictionary)
}

inline fun <UserContext : Any, Model : Any> IdentifiedResource<UserContext, *, Model>.fields(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
    : List<ResponseField<UserContext, Model, *>> = uapiResponse(fn)
