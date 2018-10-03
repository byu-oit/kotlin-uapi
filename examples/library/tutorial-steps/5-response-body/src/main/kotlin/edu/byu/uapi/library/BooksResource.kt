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
        return Library.getBook(id)
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

        }
        value(Book::title) {

        }
    }

}
