package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.BookSortableColumns
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.input.SortParams
import edu.byu.uapi.spi.input.SubsetParams

data class BookListParams(
    override val sort: SortParams<BookSortProperty>,
    override val subset: SubsetParams
) : ListParams.WithSort<BookSortProperty>,
    ListParams.WithSubset

enum class BookSortProperty(val domain: BookSortableColumns) {
    OCLC(BookSortableColumns.OCLC),
    TITLE(BookSortableColumns.TITLE),
    PUBLISHER_NAME(BookSortableColumns.PUBLISHER_NAME),
    ISBN(BookSortableColumns.ISBN),
    PUBLISHED_YEAR(BookSortableColumns.PUBLISHED_YEAR),
    AUTHOR_NAME(BookSortableColumns.FIRST_AUTHOR_NAME)
}

enum class BookSearchContext {
    TITLES,
    AUTHORS,
    GENRES,
    CONTROL_NUMBERS;
}

data class BookFilters(
    val isbn: String?,
    val title: String?,
    val subtitle: String?,
    val publisherId: Set<Int>,
    val hasAvailableCopies: Boolean?,
    val authorId: Set<Int>,
    val genre: Set<String>,
    val nested: Nested?
)

data class Nested(
    val string: String?
)
