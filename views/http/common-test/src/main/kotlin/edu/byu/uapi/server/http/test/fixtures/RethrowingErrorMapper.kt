package edu.byu.uapi.server.http.test.fixtures

import edu.byu.uapi.server.http.errors.HttpErrorMapper
import edu.byu.uapi.server.http.HttpResponse

object RethrowingErrorMapper: HttpErrorMapper {
    override fun map(ex: Throwable): HttpResponse {
        throw ex
    }
}
