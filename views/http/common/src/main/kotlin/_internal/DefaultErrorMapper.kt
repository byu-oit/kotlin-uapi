package edu.byu.uapi.server.http._internal

import com.fasterxml.jackson.core.util.BufferRecyclers
import edu.byu.uapi.server.http.HttpResponse
import edu.byu.uapi.server.http.HttpResponseBody
import edu.byu.uapi.server.http.errors.HttpErrorMapper
import edu.byu.uapi.server.spi.errors.UAPIApplicationError
import edu.byu.uapi.server.spi.errors.UAPIClientError
import edu.byu.uapi.server.spi.errors.UAPIInternalError
import edu.byu.uapi.server.spi.errors.UAPIMalformedRequestError
import edu.byu.uapi.server.spi.errors.UAPIMissingIdParamValueError
import edu.byu.uapi.server.spi.errors.UAPIUnsupportedMediaTypeError
import java.io.OutputStream

object DefaultErrorMapper : HttpErrorMapper {
    override fun map(ex: Throwable): HttpResponse {
        val (status, message, info) = when (ex) {
            is UAPIClientError -> mapClientError(ex)
            //TODO: Come up with mappings for application and internal errors
            /* is UAPIApplicationError -> mapApplicationError(ex)
             is UAPIInternalError -> mapInternalError(ex)*/
            else               -> Triple(500, "Unknown Error", listOf("Unknown Exception. See server logs for details"))
        }
        // TODO: Replace with logger

        System.err.println("Got error; mapping to HTTP status=$status message=$message validation_information=$info")
        ex.printStackTrace()

        return ErrorHttpResponse(status, message, info)
    }

}

private fun mapClientError(ex: UAPIClientError): Triple<Int, String, List<String>> {
    val (code, message) = when (ex) {
        is UAPIMissingIdParamValueError  -> 400 to "Missing Parameter"
        is UAPIMalformedRequestError     -> 400 to "Malformed Request"
        is UAPIUnsupportedMediaTypeError -> 415 to "Unsupported Media Type"
    }
    return Triple(code, message, listOf(ex.message))
}

private fun mapApplicationError(ex: UAPIApplicationError): Triple<Int, String, List<String>> {
    TODO("Want to get a way to see if we're in development mode before sending back the detailed error message")
//    return Triple(500, "Application Internal Error", )
}

private fun mapInternalError(ex: UAPIInternalError): Triple<Int, String, List<String>> {
    TODO()
}

private class ErrorHttpResponse(
    override val status: Int,
    val statusMessage: String,
    val validationInformation: List<String>
) : HttpResponse, HttpResponseBody {
    override val body: HttpResponseBody = this
    override val contentType: String = "application/json"

    override val headers: Map<String, String> = emptyMap()

    override fun writeTo(stream: OutputStream) {
        val validation = validationInformation.joinToString(",") { "\"${it.escapeForJson()}\"" }
        //We're consciously not using (much of) Jackson here so as to limit the things that can go wrong while handling errors.
        //language=JSON
        val response = """
                {
                  "metadata": {
                    "validation_response": {
                      "code": $status,
                      "message": "${statusMessage.escapeForJson()}"
                    },
                    "validation_information": [$validation]
                  }
                }
            """.trimIndent()
        stream.use { s ->
            s.writer().append(response).flush()
        }
    }
}

//Really basic, really strict JSON string escaping
private fun String.escapeForJson(): String {
    return String(BufferRecyclers.quoteAsJsonText(this))
}

