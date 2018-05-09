package edu.byu.uapidsl.http.implementation

import edu.byu.uapidsl.http.*

class PagedListGet: GetHandler {
    override fun handle(request: GetRequest): HttpResponse {
        return object : HttpResponse {
            override val status: Int = 503
            override val headers: Headers = EmptyHeaders
            override val body: ResponseBody = object : ResponseBody {
                override fun asString() = "Test Response"
            }

        }
    }

}
