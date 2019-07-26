package edu.byu.uapi.server.http.ktor

import edu.byu.uapi.server.http.engines.RequestReader
import edu.byu.uapi.server.http.path.PathFormatter
import edu.byu.uapi.server.http.path.PathFormatters
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.test.RequestReaderContractTests
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.formUrlEncode
import io.ktor.http.parametersOf
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Disabled

@Disabled("Gotta figure out how to do pathSpec parameter mapping here")
internal class KtorRequestReaderTest : RequestReaderContractTests<ApplicationCall> {
    override val reader: RequestReader<ApplicationCall>
        get() = KtorRequestReader

    override val pathFormatter: PathFormatter
        get() = PathFormatters.FLAT_CURLY_BRACE

    override fun buildRequest(
        method: String,
        requestPath: String,
        pathSpec: RoutePath,
        headers: Map<String, String>,
        queryParameters: Map<String, List<String>>,
        body: String?
    ): ApplicationCall {
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

            TODO("figure out how to do pathSpec params here")

//            val pathSpec = Parameters.build {
//                pathParameters.forEach { (k, v) -> append(k, v) }
//            }

//            CallWithPathParams(call, pathSpec)
        }
    }

    private class CallWithPathParams(
        call: TestApplicationCall,
        override val parameters: Parameters
    ) : ApplicationCall by call
}
