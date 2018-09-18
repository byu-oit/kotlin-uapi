package edu.byu.uapi.kotlin.examples.library.impl

import edu.byu.uapi.kotlin.examples.library.Book
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.response.ResponseFieldDefinition
import edu.byu.uapi.server.response.uapiResponse

/**
 * Created by Scott Hutchings on 9/17/2018.
 * kotlin-uapi-dsl-pom
 */
class BookResource : IdentifiedResource<
    MyUserContext, // user info
    Long, // id
    Book // model
    > {

    override val idType = Long::class

    override fun loadModel(userContext: MyUserContext, id: Long): Book? {
        return Library.getBook(id)
    }

    override fun canUserViewModel(userContext: MyUserContext, id: Long, model: Book): Boolean {
        return true // all books are publicly viewable
    }

    override fun idFromModel(model: Book): Long {
        return model.oclc
    }

    override val responseFields: List<ResponseFieldDefinition<MyUserContext, Book, *, *>>
        get() = uapiResponse {
            prop<Long>("oclc") {
                key = true
                getValue { book -> book.oclc }
                displayLabel = "OCLC Control Number"
            }
            prop<String>("isbn") {
                getValue { book -> book.isbn!! }
                isSystem = true
                displayLabel = "International Standard Book Number"
            }
            prop<String>("title") {
                getValue { book -> book.title }
            }
            prop<String>("subtitles") {
                getValue { book -> book.subtitles }
            }
            prop<Int>("published_year") {
                getValue { book -> book.publishedYear }
            }
            prop<String>("publisher") {
                getValue { book -> book.publisher.name }
            }
        }
}


