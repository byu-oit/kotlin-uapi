package edu.byu.uapi.spi.input

import edu.byu.uapi.model.UAPISortOrder

interface ListParams {
    object Empty : ListParams {}

    interface WithFilters<Filter : Any> : ListParams {
        val filters: Filter?

        companion object {
            const val FIELD_NAME = "filters"
        }
    }

    interface WithSort<SortableField : Enum<SortableField>> : ListParams {
        val sort: SortParams<SortableField>

        companion object {
            const val FIELD_NAME = "sort"
        }
    }

    interface WithSearch<SearchContext : Enum<SearchContext>> : ListParams {
        val search: SearchParams<SearchContext>?

        companion object {
            const val FIELD_NAME = "search"
        }
    }

    interface WithSubset : ListParams {
        val subset: SubsetParams

        companion object {
            const val FIELD_NAME = "subset"
        }
    }

}

data class ListWithTotal<T>(
    val totalItems: Int,
    val values: List<T>
) : List<T> by values

inline fun <T, R> ListWithTotal<T>.map(fn: (T) -> R): ListWithTotal<R> {
    return ListWithTotal(
        totalItems = this.totalItems,
        values = this.values.map(fn)
    )
}

inline fun <T, R> ListWithTotal<T>.flatMap(fn: (T) -> Iterable<R>): ListWithTotal<R> {
    return ListWithTotal(
        totalItems = this.totalItems,
        values = this.values.flatMap(fn)
    )
}

//TODO: Support keyed subsets (subset_key)
data class SubsetParams(
    val subsetStartOffset: Int,
    val subsetSize: Int
)

data class SortParams<SortableFields : Enum<SortableFields>>(
    val properties: List<SortableFields>,
    val order: UAPISortOrder
)

data class SearchParams<SearchContext : Enum<SearchContext>>(
    val context: SearchContext,
    val text: String
)

