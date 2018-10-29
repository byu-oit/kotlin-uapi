package edu.byu.uapi.spi.input

interface ListFilterReader<T: Any>: QueryParamReader<T, ListFilterMeta>

data class ListFilterMeta(
    override val queryParams: List<QueryParamMetadata.Param>
) : QueryParamMetadata

