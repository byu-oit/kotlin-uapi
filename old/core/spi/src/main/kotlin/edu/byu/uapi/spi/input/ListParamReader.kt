package edu.byu.uapi.spi.input

import edu.byu.uapi.model.*
import edu.byu.uapi.spi.SpecConstants
import edu.byu.uapi.spi.introspection.Introspectable
import edu.byu.uapi.spi.introspection.IntrospectionContext
import edu.byu.uapi.spi.requests.QueryParams
import java.math.BigDecimal

interface ListParamReader<Params : Any> : QueryParamReader<Params, ListParamsMeta>, Introspectable<UAPIListFeatureModel>

object EmptyListParamReader : ListParamReader<ListParams.Empty> {
    override fun introspect(context: IntrospectionContext): UAPIListFeatureModel {
        return UAPIListFeatureModel()
    }

    override fun read(input: QueryParams): ListParams.Empty = ListParams.Empty

    override fun describe(): ListParamsMeta {
        return ListParamsMeta(null, null, null, null)
    }
}

interface FilterParamsReader<FilterParams : Any> : QueryParamReader<FilterParams?, FilterParamsMeta>, Introspectable<UAPIListFiltersFeature>
interface SearchParamsReader<SearchContext: Enum<SearchContext>>: QueryParamReader<SearchParams<SearchContext>?, SearchParamsMeta>, Introspectable<UAPIListSearchFeature>
interface SortParamsReader<SortProperty: Enum<SortProperty>>: QueryParamReader<SortParams<SortProperty>, SortParamsMeta>, Introspectable<UAPIListSortFeature>

data class ListParamsMeta(
    val search: SearchParamsMeta?,
    val filter: FilterParamsMeta?,
    val sort: SortParamsMeta?,
    val subset: SubsetParamsMeta?
) : QueryParamMetadata {
    override val queryParams: List<QueryParamMetadata.Param> =
        mutableListOf<Iterable<QueryParamMetadata.Param>>(
            search?.queryParams.orEmpty(),
            filter?.queryParams.orEmpty(),
            sort?.queryParams.orEmpty(),
            subset?.queryParams.orEmpty()
        ).flatten()
}

sealed class ListParamsMetaType: QueryParamMetadata {
}

data class SearchParamsMeta(
    val contextFields: Map<String, Collection<String>>
) : ListParamsMetaType() {
    override val queryParams: List<QueryParamMetadata.Param> = listOf(
        QueryParamMetadata.Param(SEARCH_TEXT_KEY, UAPIValueType.STRING),
        QueryParamMetadata.Param(SEARCH_CONTEXT_KEY, UAPIValueType.STRING, UAPIValueConstraints(enum = contextFields.keys))
    )
    companion object {
        const val SEARCH_TEXT_KEY = SpecConstants.Collections.Query.KEY_SEARCH_TEXT
        const val SEARCH_CONTEXT_KEY = SpecConstants.Collections.Query.KEY_SEARCH_CONTEXT
    }
}

data class FilterParamsMeta(
    override val queryParams: List<QueryParamMetadata.Param>
) : ListParamsMetaType()

data class SortParamsMeta(
    val properties: List<String>,
    val defaults: List<String>,
    val defaultSortOrder: UAPISortOrder
) : ListParamsMetaType() {
    override val queryParams: List<QueryParamMetadata.Param> = listOf(
        QueryParamMetadata.Param(SORT_PROPERTIES_KEY, UAPIValueType.STRING, UAPIValueConstraints(enum = properties.toSet())),
        QueryParamMetadata.Param(SORT_ORDER_KEY, UAPIValueType.STRING, UAPIValueConstraints(enum = edu.byu.uapi.model.UAPISortOrder.values().map { it.apiValue }.toSet()))
    )
    companion object {
        const val SORT_PROPERTIES_KEY = SpecConstants.Collections.Query.KEY_SORT_PROPERTIES
        const val SORT_ORDER_KEY = SpecConstants.Collections.Query.KEY_SORT_ORDER
    }
}

data class SubsetParamsMeta(
    val defaultSize: Int,
    val maxSize: Int
) : ListParamsMetaType() {
    override val queryParams: List<QueryParamMetadata.Param> = listOf(
        QueryParamMetadata.Param(SUBSET_START_OFFSET_KEY, UAPIValueType.INT, UAPIValueConstraints(minimum = BigDecimal.ZERO)),
        QueryParamMetadata.Param(SUBSET_SIZE_KEY, UAPIValueType.INT, UAPIValueConstraints(minimum = BigDecimal.ONE, maximum = maxSize.toBigDecimal()))
    )

    companion object {
        const val SUBSET_START_OFFSET_KEY = SpecConstants.Collections.Query.KEY_SUBSET_START_OFFSET
        const val SUBSET_SIZE_KEY = SpecConstants.Collections.Query.KEY_SUBSET_SIZE
    }
}
