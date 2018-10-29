package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.scalars.ScalarFormat

interface ListParamReader<Params : Any> : QueryParamReader<Params, ListParamsMeta>

object EmptyListParamReader : ListParamReader<ListParams.Empty> {
    override fun read(input: QueryParams): ParamReadResult<ListParams.Empty> = Success(ListParams.Empty)

    override fun describe(): ListParamsMeta {
        return ListParamsMeta(null, null, null, null)
    }
}

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

sealed class ListParamsMetaType {
    abstract val queryParams: List<QueryParamMetadata.Param>
}

data class SearchParamsMeta(
    val contextFields: Map<String, Set<String>>
) : ListParamsMetaType() {
    override val queryParams: List<QueryParamMetadata.Param> = listOf(
        QueryParamMetadata.Param("search_text", ScalarFormat.STRING),
        QueryParamMetadata.Param("search_context", ScalarFormat.STRING)
    )
}

data class FilterParamsMeta(
    override val queryParams: List<QueryParamMetadata.Param>
) : ListParamsMetaType()

data class SortParamsMeta(
    val field: List<String>,
    val defaults: List<String>
) : ListParamsMetaType() {
    override val queryParams: List<QueryParamMetadata.Param> = listOf(
        QueryParamMetadata.Param("sort_properties", ScalarFormat.STRING),
        QueryParamMetadata.Param("sort_order", ScalarFormat.STRING)
    )
}

data class SubsetParamsMeta(
    val defaultSize: Int,
    val maxSize: Int
) : ListParamsMetaType() {
    override val queryParams: List<QueryParamMetadata.Param> = listOf(
        QueryParamMetadata.Param("subset_start_offset", ScalarFormat.INTEGER),
        QueryParamMetadata.Param("subset_size", ScalarFormat.INTEGER)
    )
}
