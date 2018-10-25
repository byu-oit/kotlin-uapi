package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.functional.asSuccess

interface CollectionParamsProvider<Params : Any> {
    fun getReader(): QueryParamReader<Params>
    fun getMeta(): CollectionParamsMeta
}

object EmptyCollectionParamsProvider: CollectionParamsProvider<Params.Empty> {
    override fun getReader(): QueryParamReader<Params.Empty> {
        return object : QueryParamReader<Params.Empty> {
            override fun deserializeQueryParams(values: Map<String, Set<String>>) = Params.Empty.asSuccess()
        }
    }

    override fun getMeta(): CollectionParamsMeta {
        return CollectionParamsMeta(null, null, null)
    }
}

data class CollectionParamsMeta(
    val search: SearchParamsMeta?,
    val filter: FilterParamsMeta?,
    val sort: SortParamsMeta?
)

data class SearchParamsMeta(
    val contextFields: Map<String, Set<String>>
)

data class FilterParamsMeta(
    val fields: List<FilterField>
)

data class FilterField(
    val name: String,
    val repeatable: Boolean
    //TODO: Add Type val type: Something
)

data class SortParamsMeta(
    val field: List<String>,
    val defaults: List<String>
)
