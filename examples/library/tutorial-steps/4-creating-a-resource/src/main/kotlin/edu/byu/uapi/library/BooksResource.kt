package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.Book
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.fields

class BooksResource : IdentifiedResource<LibraryUser, Long, Book> {

    override val pluralName: String = "books"

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
        value<Long>("oclc") {
            getValue { book -> book.oclc }
        }
        value<String>("title") {
            getValue { book -> book.title }
        }
    }

}
