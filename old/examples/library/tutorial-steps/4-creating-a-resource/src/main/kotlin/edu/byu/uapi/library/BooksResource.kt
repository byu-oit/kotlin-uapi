package edu.byu.uapi.library

import edu.byu.uapi.kotlin.examples.library.Book
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.kotlin.examples.library.OCLCNumber
import edu.byu.uapi.server.resources.ResourceRequestContext
import edu.byu.uapi.server.resources.list.ListResource
import edu.byu.uapi.server.resources.list.fields
import edu.byu.uapi.spi.input.ListParams

class BooksResource : ListResource.Simple<LibraryUser, OCLCNumber, Book> {

    override val pluralName: String = "books"

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
        params: ListParams.Empty
    ): List<Book> {
        return Library.listBooks(userContext.canViewRestrictedBooks).list
    }

    override val responseFields = fields {
        value<Long>("oclc") {
            getValue { book -> book.oclc.oclc }
        }
        value<String>("title") {
            getValue { book -> book.title }
        }
    }

}
