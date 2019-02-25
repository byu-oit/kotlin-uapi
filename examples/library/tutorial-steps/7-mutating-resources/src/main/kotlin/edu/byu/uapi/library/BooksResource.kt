package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.*
import edu.byu.uapi.model.UAPISortOrder
import edu.byu.uapi.server.claims.ClaimValueResult
import edu.byu.uapi.server.claims.claimConcepts
import edu.byu.uapi.server.resources.ResourceRequestContext
import edu.byu.uapi.server.resources.list.ListResource
import edu.byu.uapi.server.resources.list.fields
import edu.byu.uapi.server.types.CreateResult
import edu.byu.uapi.server.types.DeleteResult
import edu.byu.uapi.server.types.UpdateResult
import edu.byu.uapi.spi.input.ListWithTotal

class BooksResource : ListResource<LibraryUser, OCLCNumber, Book, BookListParams>,
                      ListResource.ListWithSort<LibraryUser, OCLCNumber, Book, BookListParams, BookSortProperty>,
                      ListResource.ListWithFilters<LibraryUser, OCLCNumber, Book, BookListParams, BookFilters>,
                      ListResource.ListWithSearch<LibraryUser, OCLCNumber, Book, BookListParams, BookSearchContext>,
                      ListResource.ListWithSubset<LibraryUser, OCLCNumber, Book, BookListParams>,
                      ListResource.Creatable<LibraryUser, OCLCNumber, Book, CreateBook>,
                      ListResource.Updatable<LibraryUser, OCLCNumber, Book, UpdateBook>,
                      ListResource.CreatableWithId<LibraryUser, OCLCNumber, Book, UpdateBook>,
                      ListResource.Deletable<LibraryUser, OCLCNumber, Book>,
                      ListResource.HasClaims<LibraryUser, OCLCNumber, Book> {

    override val pluralName: String = "books"
    override val scalarIdParamName: String = "oclc"

    override fun loadModel(
        requestContext: ResourceRequestContext,
        userContext: LibraryUser,
        id: OCLCNumber
    ): Book? {
        return Library.getBookByOclc(id.oclc)
    }

    override fun idFromModel(model: Book): OCLCNumber {
        return model.oclc
    }

    override fun canUserViewModel(
        requestContext: ResourceRequestContext,
        userContext: LibraryUser,
        id: OCLCNumber,
        model: Book
    ): Boolean {
        return userContext.canViewBook(model)
    }

    override fun list(
        requestContext: ResourceRequestContext,
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

    override val listDefaultSortProperties: List<BookSortProperty> =
        listOf(BookSortProperty.TITLE, BookSortProperty.OCLC)
    override val listDefaultSortOrder: UAPISortOrder = UAPISortOrder.ASCENDING
    override val listDefaultSubsetSize: Int = 50
    override val listMaxSubsetSize: Int = 100
    override fun listSearchContexts(value: BookSearchContext) = when (value) {
        BookSearchContext.TITLES          -> listOf("title", "subtitles")
        BookSearchContext.AUTHORS         -> listOf("authors.name")
        BookSearchContext.GENRES          -> listOf("genreCodes.codes", "genreCodes.name")
        BookSearchContext.CONTROL_NUMBERS -> listOf("oclc", "isbn")
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
        value(Book::publisher, Publisher::id) {
            displayLabel = "Publisher"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }

            description(Publisher::commonName)
            longDescription(Publisher::fullName)
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

    override fun canUserCreate(
        requestContext: ResourceRequestContext,
        userContext: LibraryUser
    ): Boolean {
        return userContext.canCreateBooks
    }

    override fun handleCreate(
        requestContext: ResourceRequestContext,
        userContext: LibraryUser,
        input: CreateBook
    ): CreateResult<Book> {
        val publisher = Library.getPublisher(input.publisherId)
            ?: return CreateResult.InvalidInput("publisher_id", "No such publisher exists")
        val authors = input.authorIds.map {
            Library.getAuthor(it) ?: return CreateResult.InvalidInput("author_ids", "No such author exists")
        }
        val genres = input.genreCodes.map {
            Library.getGenreByCode(it) ?: return CreateResult.InvalidInput("genre_codes", "No such genre exists")
        }

        val created = Library.createBook(
            NewBook(
                oclc = input.oclc,
                isbn = input.isbn,
                title = input.title,
                subtitles = input.subtitles,
                publishedYear = input.publishedYear.value,
                publisher = publisher,
                authors = authors,
                genres = genres,
                restricted = input.restricted
            )
        )

        return CreateResult.Success(created)
    }

    override fun canUserUpdate(
        requestContext: ResourceRequestContext,
        userContext: LibraryUser,
        id: OCLCNumber,
        model: Book
    ): Boolean {
        return userContext.canModifyBooks
    }

    override fun canBeUpdated(
        id: OCLCNumber,
        model: Book
    ): Boolean {
        return true
    }

    override fun handleUpdate(
        requestContext: ResourceRequestContext,
        userContext: LibraryUser,
        id: OCLCNumber,
        model: Book,
        input: UpdateBook
    ): UpdateResult<Book> {
        val publisher = Library.getPublisher(input.publisherId)
            ?: return UpdateResult.InvalidInput("publisher_id", "No such publisher exists")
        val authors = input.authorIds.map {
            Library.getAuthor(it) ?: return UpdateResult.InvalidInput("author_ids", "No such author exists")
        }
        val genres = input.genreCodes.map {
            Library.getGenreByCode(it) ?: return UpdateResult.InvalidInput("genre_codes", "No such genre exists")
        }

        val updated = Library.updateBook(
            NewBook(
                oclc = id.oclc,
                isbn = input.isbn,
                title = input.title,
                subtitles = input.subtitles,
                publishedYear = input.publishedYear.value,
                publisher = publisher,
                authors = authors,
                genres = genres,
                restricted = input.restricted
            )
        )

        return UpdateResult.Success(updated)
    }

    override fun canUserCreateWithId(
        requestContext: ResourceRequestContext,
        userContext: LibraryUser,
        id: OCLCNumber
    ): Boolean {
        return userContext.canCreateBooks
    }

    override fun handleCreateWithId(
        requestContext: ResourceRequestContext,
        userContext: LibraryUser,
        id: OCLCNumber,
        input: UpdateBook
    ): CreateResult<Book> {
        val publisher = Library.getPublisher(input.publisherId)
            ?: return CreateResult.InvalidInput("publisher_id", "No such publisher exists")
        val authors = input.authorIds.map {
            Library.getAuthor(it) ?: return CreateResult.InvalidInput("author_ids", "No such author exists")
        }
        val genres = input.genreCodes.map {
            Library.getGenreByCode(it) ?: return CreateResult.InvalidInput("genre_codes", "No such genre exists")
        }

        val created = Library.createBook(
            NewBook(
                oclc = id.oclc,
                isbn = input.isbn,
                title = input.title,
                subtitles = input.subtitles,
                publishedYear = input.publishedYear.value,
                publisher = publisher,
                authors = authors,
                genres = genres,
                restricted = input.restricted
            )
        )

        return CreateResult.Success(created)
    }

    override fun canUserDelete(
        requestContext: ResourceRequestContext,
        userContext: LibraryUser,
        id: OCLCNumber,
        model: Book
    ): Boolean {
        return userContext.canDeleteBooks
    }

    override fun canBeDeleted(
        id: OCLCNumber,
        model: Book
    ): Boolean {
        val copies = Library.hasCheckedOutCopies(model.id)
        return !copies //Cannot delete books that have checked-out copies!
    }

    override fun handleDelete(
        requestContext: ResourceRequestContext,
        userContext: LibraryUser,
        id: OCLCNumber,
        model: Book
    ): DeleteResult {
        Library.deleteBook(model.id)
        return DeleteResult.Success
    }

    override fun canUserMakeAnyClaims(
        requestContext: ResourceRequestContext,
        user: LibraryUser,
        subject: OCLCNumber,
        subjectModel: Book
    ): Boolean {
        return true
    }

    override val claimConcepts = claimConcepts {
        concept<String>("foo") {
            canUserMakeClaim { requestContext, user, model -> true }
            getValue { ClaimValueResult.Value("foo") }
//            compareUsing {
//                it.length
//            }
//            supports = EnumSet.of(UAPIClaimRelationship.GREATER_THAN)
        }
        concept(Book::title) {
            canUserMakeClaim { _, _, _ -> true }
        }
    }
}
