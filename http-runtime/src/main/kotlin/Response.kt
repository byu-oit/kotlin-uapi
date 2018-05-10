package edu.byu.uapidsl.http

import com.fasterxml.jackson.databind.ObjectMapper
import edu.byu.uapidsl.types.UAPIResponse

interface HttpResponse {

    val status: Int
    val headers: Headers
    val body: ResponseBody

}

interface ResponseBody {

    fun asString(): String

}


class ErrorHttpResponse(val error: HttpError, objectMapper: ObjectMapper)
    : UAPIHttpResponse(error.toResponse(), objectMapper) {

    override val status = error.code

    override val headers: Headers
        get() = SimpleHeaders(super.headers + mapOf("X-BYUAPI-Error" to setOf(error.message)))

}

open class UAPIHttpResponse(uapiResponse: UAPIResponse<*>, jsonMapper: ObjectMapper) : HttpResponse {

    private val validationResponse = uapiResponse.metadata.validationResponse

    override val status = validationResponse.code

    override val headers: Headers = SimpleHeaders(
        "X-BYUAPI-Response-Message" to validationResponse.message,
        "Content-Type" to "application/json"
    )

    override val body: ResponseBody = JacksonResponseBody(uapiResponse, jsonMapper)

}

class JacksonResponseBody(val body: UAPIResponse<*>, val mapper: ObjectMapper) : ResponseBody {
    override fun asString(): String {
        return mapper.writeValueAsString(body)
    }
}

