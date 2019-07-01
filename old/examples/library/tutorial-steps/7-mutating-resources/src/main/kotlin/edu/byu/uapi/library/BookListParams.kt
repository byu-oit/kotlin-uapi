package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.*
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.input.SearchParams
import edu.byu.uapi.spi.input.SortParams
import edu.byu.uapi.spi.input.SubsetParams

data class BookListParams(
    override val sort: SortParams<BookSortProperty>,
    override val filters: BookFilters?,
    override val search: SearchParams<BookSearchContext>?,
    override val subset: SubsetParams
) : ListParams.WithSort<BookSortProperty>,
    ListParams.WithFilters<BookFilters>,
    ListParams.WithSearch<BookSearchContext>,
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

fun BookSearchContext.toDomain(searchText: String) = when(this) {
    BookSearchContext.TITLES -> BookTitleSearch(searchText)
    BookSearchContext.AUTHORS -> BookAuthorSearch(searchText)
    BookSearchContext.GENRES -> BookGenreSearch(searchText)
    BookSearchContext.CONTROL_NUMBERS -> BookControlNumbersSearch(searchText)
}

data class BookFilters(
    val isbns: Set<String>,
    val title: String?,
    val subtitle: String?,
    val publisherIds: Set<Int>,
    val publisherNames: Set<String>,
    val publicationYear: Int?,
    val restricted: Boolean?,
    val authors: AuthorFilters?,
    val genres: GenreFilters?
) {
    fun toDomain() = if (hasAnyValues) {
        BookQueryFilters(
            isbn = isbns,
            title = title,
            subtitle = subtitle,
            publisherId = publisherIds,
            publisherNames = publisherNames,
            publicationYear = publicationYear,
            restricted = restricted,
            authors = authors?.toDomain(),
            genres = genres?.toDomain()
        )
    } else {
        null
    }

    val hasAnyValues =
        isbns.isNotEmpty()
            || title != null
            || subtitle != null
            || publisherIds.isNotEmpty()
            || publisherNames.isNotEmpty()
            || publicationYear != null
            || restricted != null
            || (authors == null || authors.hasAnyValues)
            || (genres == null || genres.hasAnyValues)
}

data class AuthorFilters(
    val ids: Set<Int>,
    val names: Set<String>
) {
    fun toDomain() = if (hasAnyValues) {
        AuthorQueryFilters(
            id = ids,
            name = names
        )
    } else {
        null
    }

    val hasAnyValues = ids.isNotEmpty() || names.isNotEmpty()
}

data class GenreFilters(
    val codes: Set<String>,
    val names: Set<String>
) {
    fun toDomain() = if (hasAnyValues) {
        GenreQueryFilters(
            code = codes,
            name = names
        )
    } else {
        null
    }

    val hasAnyValues = codes.isNotEmpty() || names.isNotEmpty()
}
