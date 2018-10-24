package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.dictionary.TypeDictionary

interface CollectionParamsProvider<Params : Any> {
    fun getReader(dictionary: TypeDictionary): QueryParamReader<Params>
    fun getMeta(dictionary: TypeDictionary): CollectionParamsMeta
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
