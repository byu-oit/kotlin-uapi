package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.Book
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.fields
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
        valueArray<String>("genres") {
            getValues { book -> book.genres.map { it.code } }
            description { book, genres, value, index -> book.genres[index].name }
            displayLabel = "Genre"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
        valueArray<Int>("author_ids") {
            getValues { book -> book.authors.map { it.authorId } }
            description { book, authors, value, index -> book.authors[index].name }
            displayLabel = "Author"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }
//        valueArray(Book::authors) {
//            description { book, item, index -> item.name }
//        }

//        valueArray(Book::authors, Author::id) {
//            description = Author::name
//            longDescription = Author::name
//            description { book, author -> author.name }
//        }

        value(Book::publishedYear) {
            displayLabel = "Publication Year"
            doc = "The year the book was published"
            modifiable { libraryUser, book, value -> libraryUser.canModifyBooks }
        }

    }
}
