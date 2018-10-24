package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.Author
import edu.byu.uapi.kotlin.examples.library.Book
import edu.byu.uapi.kotlin.examples.library.Genre
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.fields
import edu.byu.uapi.spi.annotations.CollectionParams
import edu.byu.uapi.spi.annotations.DefaultSort
import edu.byu.uapi.spi.annotations.SearchFields
import edu.byu.uapi.spi.input.BetweenInclusive
import edu.byu.uapi.spi.input.Params
import java.time.Year
import kotlin.reflect.KClass

@CollectionParams
interface BookListParams :
    Params.Filtering<BookFilters>,
    Params.Searching<BookSearchContext>,
    Params.Sorting<BookSortField>

enum class BookSearchContext {
    @SearchFields("title", "subtitles")
    titles,
    @SearchFields("authors.name")
    authors,
    @SearchFields("genres.name", "genres.code")
    genres,
    @SearchFields("oclc", "isbn")
    control_numbers;
}

enum class BookSortField {
    @DefaultSort(order = 2)
    oclc,
    @DefaultSort(order = 1)
    title,
    publisher_id,
    publisher_name,
    isbn,
    published_year
}

interface BookFilters{
    val isbn: String?
    val title: String?
    val subtitle: String?
    val publisherId: Set<Int>
    val hasAvailableCopies: Boolean?
    val authorId: Set<Int>
    val genre: Set<String>
    val published: BetweenInclusive<Year>
}


class BooksResource : IdentifiedResource<LibraryUser, Long, Book>
                      , IdentifiedResource.Listable<LibraryUser, Long, Book, BookListParams>
{
    override fun list(
        userContext: LibraryUser,
        params: BookListParams
    ): Collection<Book> {
        TODO("not implemented")
    }

    override val paramsType: KClass<BookListParams>
        get() = TODO("not implemented")

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
