package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.test.HttpRequestContractTest
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.formUrlEncode
import io.ktor.http.parametersOf
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication

internal class KtorRequestAdapterTest : HttpRequestContractTest {
    override fun buildRequest(
        method: String,
        headers: Map<String, String>,
        queryParameters: Map<String, List<String>>,
        pathParameters: Map<String, String>,
        body: String?
    ): HttpRequest {
        return withTestApplication {
            val call = createCall {
                this.method = HttpMethod.parse(method)
                headers.forEach { (k, v) -> addHeader(k, v) }

                if (queryParameters.isNotEmpty()) {
                    val qp = parametersOf(*queryParameters.toList().toTypedArray())

                    uri = "?" + qp.formUrlEncode()
                }

                if (body != null) {
                    setBody(body)
                }
            }

            val path = Parameters.build {
                pathParameters.forEach { (k, v) -> append(k, v) }
            }

            KtorRequestAdapter(
                CallWithPathParams(call, path)
            )
        }
    }

    private class CallWithPathParams(
        call: TestApplicationCall,
        override val parameters: Parameters
    ) : ApplicationCall by call
}
