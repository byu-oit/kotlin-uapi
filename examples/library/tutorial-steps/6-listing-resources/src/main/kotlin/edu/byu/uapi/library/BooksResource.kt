package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.Author
import edu.byu.uapi.kotlin.examples.library.Book
import edu.byu.uapi.kotlin.examples.library.Genre
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.fields
import edu.byu.uapi.spi.input.CollectionParams
import edu.byu.uapi.spi.input.SearchParams
import edu.byu.uapi.spi.input.SortParams
import kotlin.reflect.KClass

data class BookListParams(
    override val filter: BookFilters?,
    override val sort: SortParams<BookSortField>,
    override val search: SearchParams<BookSearchContext>?
) : CollectionParams.Filtering<BookFilters>,
    CollectionParams.Searching<BookSearchContext>,
    CollectionParams.Sorting<BookSortField> {
    companion object
        : CollectionParams.Filtering.Companion<BookFilters>,
          CollectionParams.Searching.Companion<BookSearchContext>,
          CollectionParams.Sorting.Companion<BookSortField> {
        override val filterType: KClass<BookFilters> = BookFilters::class
        override val defaultSortFields: List<BookSortField> = listOf(BookSortField.OCLC)
        override val searchContextFields: Map<BookSearchContext, Collection<String>> = mapOf()
    }
}

enum class BookSearchContext {

}

enum class BookSortField {
    OCLC
}

data class BookFilters(
    val isbn: String?,
    val title: String?,
    val subtitle: String?,
    val publisherId: Set<Int>,
    val hasAvailableCopies: Boolean,
    val authorId: Set<Int>,
    val genre: Set<String>,
    val published: IntRange
)

class BooksResource : IdentifiedResource<LibraryUser, Long, Book>
//                      , IdentifiedResource.Listable<LibraryUser, Long, Book, BookListParams>
{

    override val idType: KClass<Long> = Long::class

    override fun loadModel(
        userContext: LibraryUser,
        id: Long
    ): Book? {
        return Library.getBookByOclc(id)
    }

    override fun idFromModel(model: Book): Long {
        return model.oclc
    }

    override fun canUserViewModel(
        userContext: LibraryUser,
        id: Long,
        model: Book
    ): Boolean {
        return true
    }

    override val responseFields = fields {
        value(Book::oclc) {
            key = true
            displayLabel = "OCLC Control Number"
            doc = "Control number assigned to this title by the [Online Computer Library Center](www.oclc.org)."
        }
        value(Book::title) {
            displayLabel = "Title"
            doc = "The main title of the book"
            modifiable { libraryUser, book, title -> libraryUser.canModifyBooks }
        }
        value<Int>("publisher_id") {
            getValue { book -> book.publisher.publisherId }
            displayLabel = "Publisher"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }

            description { book, publisherId -> book.publisher.commonName }
            longDescription { book, publisherId -> book.publisher.fullName }
        }
        value(Book::availableCopies) {
            isDerived = true
            displayLabel = "Available Copies"
        }
        nullableValue(Book::isbn) {
            isSystem = true
            displayLabel = "ISBN"
            doc = "International Standard Book Number"
        }
        valueArray(Book::subtitles) {
            displayLabel = "Subtitles"
            doc = "The book's subtitles, if any"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
        mappedValueArray("author_ids", Book::authors, Author::authorId) {
            description(Author::name)
            displayLabel = "Author(s)"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
        mappedValueArray(Book::genres, Genre::code) {
            displayLabel = "Genre(s)"
            description(Genre::name)
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
        value(Book::publishedYear) {
            displayLabel = "Publication Year"
            doc = "The year the book was published"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
    }
}
