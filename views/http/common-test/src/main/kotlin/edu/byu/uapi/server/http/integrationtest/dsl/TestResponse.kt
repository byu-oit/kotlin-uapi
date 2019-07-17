package edu.byu.uapi.server.http.integrationtest.dsl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import edu.byu.uapi.server.http.HTTP_NO_CONTENT
import edu.byu.uapi.server.http.HTTP_OK
import edu.byu.uapi.server.http.engines.HttpResponse
import edu.byu.uapi.server.http.engines.HttpResponseBody
import java.io.OutputStream

val jackson = ObjectMapper().registerKotlinModule()

sealed class TestResponse(
    override val status: Int,
    override val headers: Map<String, String>
) : HttpResponse {

    @Suppress("FunctionName")
    companion object {
        fun Json(body: String, status: Int = HTTP_OK, headers: Map<String, String> = emptyMap()) =
            Body(body.toByteArray(), "application/json", status, headers)

        fun Json(body: Any, status: Int = HTTP_OK, headers: Map<String, String> = emptyMap()) =
            Body(jackson.writeValueAsBytes(body), "application/json", status, headers)

        fun Text(body: String, status: Int = HTTP_OK, headers: Map<String, String> = emptyMap()) =
            Body(body.toByteArray(), "text/plain", status, headers)
    }

    class Empty(
        status: Int = HTTP_NO_CONTENT,
        headers: Map<String, String> = emptyMap()
    ) : TestResponse(status, headers) {
        override val body: HttpResponseBody? = null
    }

    class Body(
        val bodyBytes: ByteArray,
        override val contentType: String,
        status: Int = HTTP_OK,
        headers: Map<String, String> = emptyMap()
    ) : TestResponse(status, headers + ("Content-Type" to contentType)),
        HttpResponseBody {
        constructor(
            bodyString: String,
            contentType: String,
            status: Int = HTTP_OK,
            headers: Map<String, String> = emptyMap()
        ) : this(bodyString.toByteArray(), contentType, status, headers)

        override val body: HttpResponseBody? = this
        override fun writeTo(stream: OutputStream) {
            stream.write(bodyBytes)
        }
    }
}
