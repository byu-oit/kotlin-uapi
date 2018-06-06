package edu.byu.uapidsl.http

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.types.UAPIResponse

interface HttpResponse {

    val status: Int
    val headers: Headers
    val body: ResponseBody

}

interface ResponseBody {

    fun asString(): String

}


class ErrorHttpResponse(val error: HttpError, jsonWriter: ObjectWriter)
    : UAPIHttpResponse(error.toResponse(), jsonWriter) {

    override val status = error.code

    override val headers: Headers
        get() = SimpleHeaders(super.headers + mapOf("X-BYUAPI-Error" to setOf(error.message)))

}

open class UAPIHttpResponse(uapiResponse: UAPIResponse<*>, jsonWriter: ObjectWriter) : HttpResponse {

    private val validationResponse = uapiResponse.metadata.validationResponse

    override val status = validationResponse.code

    override val headers: Headers = SimpleHeaders(
        "X-BYUAPI-Response-Message" to validationResponse.message,
        "Content-Type" to "application/json"
    )

    override val body: ResponseBody = JacksonResponseBody(uapiResponse, jsonWriter)

}

object EmptyUAPIHttpResponse: HttpResponse {
    override val status: Int = 204
    override val headers: Headers = EmptyHeaders
    override val body: ResponseBody = EmptyResponseBody
}

object EmptyResponseBody: ResponseBody {
    override fun asString(): String {
        return ""
    }
}

class JacksonResponseBody(val body: UAPIResponse<*>, val writer: ObjectWriter) : ResponseBody {
    override fun asString(): String {
        return writer.writeValueAsString(body)
    }
}

