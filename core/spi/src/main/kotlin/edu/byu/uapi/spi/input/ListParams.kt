package edu.byu.uapi.spi.input

interface ListParams {
    object Empty : ListParams {}

    interface Filtering<Filter : Any> : ListParams {
        val filter: Filter?

        companion object {
            const val FIELD_NAME = "filter"
        }
    }

    interface Sorting<SortableField : Enum<SortableField>> : ListParams {
        val sort: SortParams<SortableField>

        companion object {
            const val FIELD_NAME = "sort"
        }
    }

    interface Searching<SearchContext : Enum<SearchContext>> : ListParams {
        val search: SearchParams<SearchContext>?

        companion object {
            const val FIELD_NAME = "search"
        }
    }

    interface SubSetting : ListParams {
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

interface BetweenInclusive<Type> {
    val start: Type?
    val end: Type?
}

interface BetweenExclusive<Type> {
    val start: Type?
    val end: Type?
}

data class SortParams<SortableFields : Enum<SortableFields>>(
    val fields: List<SortableFields>,
    val order: SortOrder
)

data class SearchParams<SearchContext : Enum<SearchContext>>(
    val context: SearchContext,
    val text: String
)

enum class SortOrder {
    ASCENDING, DESCENDING;

    override fun toString(): String = name.toLowerCase()

}
//
//enum class PersonSearchContexts {
//    NAME, IDENTIFIERS
//}
//
//enum class PersonSortParams {
//    BYU_ID, SORT_NAME, BIRTHDAY
//}
//
//data class PersonSearchFilters(
//    val byuId: String?,
//    val birthday: LocalDate?
//) {
//}
//
////TODO: Delete this
//data class PersonSearchParams(
//    override val filter: PersonSearchFilters?,
//    override val sort: SortParams<PersonSortParams>,
//    override val search: SearchParams<PersonSearchContexts>?
//) : Filtering<PersonSearchFilters>,
//    Searching<PersonSearchContexts>,
//    Sorting<PersonSortParams> {
//
//    companion object :
//        Filtering.Companion<PersonSearchFilters>,
//        Searching.Companion<PersonSearchContexts>,
//        Sorting.Companion<PersonSortParams> {
//        override val filterType: KClass<PersonSearchFilters> = PersonSearchFilters::class
//        override val defaultSortFields: List<PersonSortParams> = listOf(PersonSortParams.BYU_ID)
//        override val searchContextFields: Map<PersonSearchContexts, Collection<String>> = mapOf(
//            PersonSearchContexts.IDENTIFIERS to setOf("byu_id", "person_id"),
//            PersonSearchContexts.NAME to setOf("sort_name", "preferred_first_name")
//        )
//    }
//
//}
