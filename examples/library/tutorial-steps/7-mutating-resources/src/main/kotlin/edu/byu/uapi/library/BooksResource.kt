package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.*
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.fields
import edu.byu.uapi.spi.input.ListWithTotal
import edu.byu.uapi.spi.input.UAPISortOrder
import edu.byu.uapi.spi.validation.Validating

class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
                      IdentifiedResource.Listable.WithSort<LibraryUser, Long, Book, BookListParams, BookSortProperty>,
                      IdentifiedResource.Listable.WithFilters<LibraryUser, Long, Book, BookListParams, BookFilters>,
                      IdentifiedResource.Listable.WithSearch<LibraryUser, Long, Book, BookListParams, BookSearchContext>,
                      IdentifiedResource.Listable.WithSubset<LibraryUser, Long, Book, BookListParams>,
                      IdentifiedResource.Creatable<LibraryUser, Long, Book, CreateBook> {

    override fun list(
        userContext: LibraryUser,
        params: BookListParams
    ): ListWithTotal<Book> {
        val search = params.search?.run { context.toDomain(text) }
        val result = Library.listBooks(
            includeRestricted = userContext.canViewRestrictedBooks,
            sortColumns = params.sort.properties.map { it.domain },
            sortAscending = params.sort.order == UAPISortOrder.ASCENDING,
            filters = params.filters?.toDomain(),
            search = search,
            subsetSize = params.subset.subsetSize,
            subsetStart = params.subset.subsetStartOffset
        )
        return ListWithTotal(
            totalItems = result.totalItems,
            values = result.list
        )
    }

    override val listDefaultSortProperties: List<BookSortProperty> = listOf(BookSortProperty.TITLE, BookSortProperty.OCLC)
    override val listDefaultSortOrder: UAPISortOrder = UAPISortOrder.ASCENDING
    override val listDefaultSubsetSize: Int = 50
    override val listMaxSubsetSize: Int = 100
    override fun listSearchContexts(value: BookSearchContext) = when (value) {
        BookSearchContext.TITLES -> listOf("title", "subtitles")
        BookSearchContext.AUTHORS -> listOf("authors.name")
        BookSearchContext.GENRES -> listOf("genreCodes.codes", "genreCodes.name")
        BookSearchContext.CONTROL_NUMBERS -> listOf("oclc", "isbn")
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
        return userContext.canViewBook(model)
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
        value(Book::restricted) {
            displayLabel = "Is Restricted"
            doc = "Whether the book is shelved in the Restricted Section"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
    }

    override fun canUserCreate(userContext: LibraryUser): Boolean {
        return userContext.canCreateBooks
    }

    override fun validateCreateInput(
        userContext: LibraryUser,
        input: CreateBook,
        validation: Validating
    ) {
        TODO("not implemented")
    }

    override fun handleCreate(
        userContext: LibraryUser,
        input: CreateBook
    ): Long {
        val bookId = Library.createBook(NewBook(
            oclc = input.oclc,
            isbn = input.isbn,
            title = input.title,
            subtitles = input.subtitles,
            publishedYear = input.publishedYear.value,
            publisher = Library.getPublisher(input.publisherId)!!,
            authors = input.authorIds.map { Library.getAuthor(it)!! },
            genres = input.genreCodes.map { Library.getGenreByCode(it)!! },
            restricted = input.restricted
        ))
        return input.oclc
    }

}
