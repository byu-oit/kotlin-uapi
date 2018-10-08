package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.Book
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.server.response.uapiResponse
import kotlin.reflect.KClass

class BooksResource : IdentifiedResource<LibraryUser, Long, Book> {

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

    override val responseFields: List<ResponseField<LibraryUser, Book, *>> = uapiResponse {
        value(Book::oclc) {
            key = true
            isSystem = true
            displayLabel = "OCLC Control Number"
            doc = """OCLC Control Number
                | Control number assigned to this title by the Online Computer Library Center (www.oclc.org).
            """.trimMargin()
        }
        nullableValue(Book::isbn) {
            isSystem = true
            displayLabel = "ISBN"
            doc = """ISBN
                | International Standard Book Number
            """.trimMargin()
        }
        value(Book::title) {
            displayLabel = "Title"
            doc = "The main title of the book"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
        valueArray(Book::subtitles) {
            displayLabel = "Subtitles"
            doc = "The book's subtitles, if any"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
        value(Book::publishedYear) {
            displayLabel = "Publication Year"
            doc = "The year the book was published"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
        value<Int>("publisher_id") {
            getValue { book -> book.publisher.publisherId }
            description { book, value -> book.publisher.name }
            displayLabel = "Publisher"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
        valueArray<Int>("author_ids") {
            getValues { book -> book.authors.map { it.authorId } }
            description { book, item, index -> book.authors[index].name }
            displayLabel = "Author"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
        valueArray<String>("genres") {
            getValues { book -> book.genres.map { it.code } }
            description { book, item, index -> book.genres[index].name }
            displayLabel = "Genre"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
        value<Int>("available_copies") {
            isDerived = true
            getValue { book -> book.availableCopies }
            displayLabel = "Available Copies"
        }
    }
}
