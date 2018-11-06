package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.Author
import edu.byu.uapi.kotlin.examples.library.Book
import edu.byu.uapi.kotlin.examples.library.Genre
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.fields
import edu.byu.uapi.spi.input.*
import java.time.Year
import kotlin.reflect.KClass

//interface BookListParams
//    ListParams.Filtering<BookFilters>,
//    ListParams.Searching<BookSearchContext>,
//    ListParams.Sorting<BookSortField>

data class BookListParams(
    override val sort: SortParams<BookSortField>,
    override val filter: BookFilters?,
    override val subset: SubsetParams,
    override val search: SearchParams<BookSearchContext>?
) : ListParams.Sorting<BookSortField>,
    ListParams.Filtering<BookFilters>,
    ListParams.SubSetting,
    ListParams.Searching<BookSearchContext>

enum class BookSearchContext {
    titles,
    authors,
    genres,
    control_numbers;
}

enum class BookSortField {
    oclc,
    title,
    publisher_id,
    publisher_name,
    isbn,
    published_year
}

interface BookFilters {
    val isbn: String?
    val title: String?
    val subtitle: String?
    val publisherId: Set<Int>
    val hasAvailableCopies: Boolean?
    val authorId: Set<Int>
    val genre: Set<String>
    val published: BetweenInclusive<Year>
}

class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
                      IdentifiedResource.Listable.WithSort<LibraryUser, Long, Book, BookListParams, BookSortField>,
                      IdentifiedResource.Listable.WithFilters<LibraryUser, Long, Book, BookListParams, BookFilters>,
                      IdentifiedResource.Listable.WithSubset<LibraryUser, Long, Book, BookListParams>,
                      IdentifiedResource.Listable.WithSearch<LibraryUser, Long, Book, BookListParams, BookSearchContext>
{

    override fun list(
        userContext: LibraryUser,
        params: BookListParams
    ): ListWithTotal<Book> {
        TODO("not implemented")
    }

    override val listParamsType: KClass<BookListParams> = BookListParams::class
    override val listDefaultSortProperties: List<BookSortField> = listOf(BookSortField.title, BookSortField.oclc)
    override val listDefaultSortOrder: SortOrder = SortOrder.ASCENDING
    override val listDefaultSubsetSize: Int = 50
    override val listMaxSubsetSize: Int = 100
    override fun listSearchContexts(value: BookSearchContext): Collection<String> = when(value) {
        BookSearchContext.titles -> listOf("title", "subtitles")
        BookSearchContext.authors -> listOf("authors.name")
        BookSearchContext.genres -> listOf("genres.code", "genres.name")
        BookSearchContext.control_numbers -> listOf("oclc", "isbn")
    }

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
