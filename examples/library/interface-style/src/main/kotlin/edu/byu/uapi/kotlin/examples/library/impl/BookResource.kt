package edu.byu.uapi.kotlin.examples.library.impl

import edu.byu.uapi.kotlin.examples.library.Book
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.response.ResponseField
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

    override fun loadModel(
        userContext: MyUserContext,
        id: Long
    ): Book? {
        return Library.getBookByOclc(id)
    }

    override fun canUserViewModel(
        userContext: MyUserContext,
        id: Long,
        model: Book
    ): Boolean {
        return true // all books are publicly viewable
    }

    override fun idFromModel(model: Book): Long {
        return model.oclc
    }

    override val responseFields: List<ResponseField<MyUserContext, Book, *>>
        get() = uapiResponse {
            value(Book::oclc) {
                key = true
                displayLabel = "OCLC Control Number"
            }
            nullableValue(Book::isbn) {
                isSystem = true
                displayLabel = "International Standard Book Number"
            }
            value(Book::title) {
            }
            nullableValue(Book::subtitles) {
            }
            value(Book::publishedYear) {
            }
            value<Int>("publisher") {
                getValue { book -> book.publisher.id }
                description { book, publisherId -> book.publisher.commonName }
            }
        }
}


