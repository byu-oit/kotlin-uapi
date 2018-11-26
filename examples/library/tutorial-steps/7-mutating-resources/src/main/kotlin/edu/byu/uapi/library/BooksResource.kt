package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.*
import edu.byu.uapi.server.resources.identified.*
import edu.byu.uapi.spi.input.ListWithTotal
import edu.byu.uapi.spi.input.UAPISortOrder

class BooksResource : IdentifiedResource<LibraryUser, Long, Book>,
                      IdentifiedResource.Listable.WithSort<LibraryUser, Long, Book, BookListParams, BookSortProperty>,
                      IdentifiedResource.Listable.WithFilters<LibraryUser, Long, Book, BookListParams, BookFilters>,
                      IdentifiedResource.Listable.WithSearch<LibraryUser, Long, Book, BookListParams, BookSearchContext>,
                      IdentifiedResource.Listable.WithSubset<LibraryUser, Long, Book, BookListParams>,
                      IdentifiedResource.Creatable<LibraryUser, Long, Book, CreateBook>,
                      IdentifiedResource.Updatable<LibraryUser, Long, Book, UpdateBook>,
                      IdentifiedResource.CreatableWithId<LibraryUser, Long, Book, UpdateBook>,
                      IdentifiedResource.Deletable<LibraryUser, Long, Book> {

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

    override fun canUserCreate(userContext: LibraryUser): Boolean {
        return userContext.canCreateBooks
    }

    override fun handleCreate(
        userContext: LibraryUser,
        input: CreateBook
    ): CreateResult<Long> {
        val publisher = Library.getPublisher(input.publisherId)
            ?: return CreateResult.InvalidInput("publisher_id", "No such publisher exists")
        val authors = input.authorIds.map {
            Library.getAuthor(it) ?: return CreateResult.InvalidInput("author_ids", "No such author exists")
        }
        val genres = input.genreCodes.map {
            Library.getGenreByCode(it) ?: return CreateResult.InvalidInput("genre_codes", "No such genre exists")
        }

        Library.createBook(NewBook(
            oclc = input.oclc,
            isbn = input.isbn,
            title = input.title,
            subtitles = input.subtitles,
            publishedYear = input.publishedYear.value,
            publisher = publisher,
            authors = authors,
            genres = genres,
            restricted = input.restricted
        ))

        return CreateResult.Success(input.oclc)
    }

    override fun canUserUpdate(
        userContext: LibraryUser,
        id: Long,
        model: Book
    ): Boolean {
        return userContext.canModifyBooks
    }

    override fun canBeUpdated(
        id: Long,
        model: Book
    ): Boolean {
        return true
    }

    override fun handleUpdate(
        userContext: LibraryUser,
        id: Long,
        model: Book,
        input: UpdateBook
    ): UpdateResult {
        val publisher = Library.getPublisher(input.publisherId)
            ?: return UpdateResult.InvalidInput("publisher_id", "No such publisher exists")
        val authors = input.authorIds.map {
            Library.getAuthor(it) ?: return UpdateResult.InvalidInput("author_ids", "No such author exists")
        }
        val genres = input.genreCodes.map {
            Library.getGenreByCode(it) ?: return UpdateResult.InvalidInput("genre_codes", "No such genre exists")
        }

        Library.updateBook(NewBook(
            oclc = id,
            isbn = input.isbn,
            title = input.title,
            subtitles = input.subtitles,
            publishedYear = input.publishedYear.value,
            publisher = publisher,
            authors = authors,
            genres = genres,
            restricted = input.restricted
        ))

        return UpdateResult.Success
    }

    override fun canUserCreateWithId(
        userContext: LibraryUser,
        id: Long
    ): Boolean {
        return userContext.canCreateBooks
    }

    override fun handleCreateWithId(
        userContext: LibraryUser,
        input: UpdateBook,
        id: Long
    ): CreateResult<Long> {
        val publisher = Library.getPublisher(input.publisherId)
            ?: return CreateResult.InvalidInput("publisher_id", "No such publisher exists")
        val authors = input.authorIds.map {
            Library.getAuthor(it) ?: return CreateResult.InvalidInput("author_ids", "No such author exists")
        }
        val genres = input.genreCodes.map {
            Library.getGenreByCode(it) ?: return CreateResult.InvalidInput("genre_codes", "No such genre exists")
        }

        Library.createBook(NewBook(
            oclc = id,
            isbn = input.isbn,
            title = input.title,
            subtitles = input.subtitles,
            publishedYear = input.publishedYear.value,
            publisher = publisher,
            authors = authors,
            genres = genres,
            restricted = input.restricted
        ))

        return CreateResult.Success(id)
    }

    override fun canUserDelete(
        userContext: LibraryUser,
        id: Long,
        model: Book
    ): Boolean {
        return userContext.canDeleteBooks
    }

    override fun canBeDeleted(
        id: Long,
        model: Book
    ): Boolean {
        val copies = Library.hasCheckedOutCopies(model.id)
        return !copies //Cannot delete books that have checked-out copies!
    }

    override fun handleDelete(
        userContext: LibraryUser,
        id: Long,
        model: Book
    ): DeleteResult {
        Library.deleteBook(model.id)
        return DeleteResult.Success
    }
}
