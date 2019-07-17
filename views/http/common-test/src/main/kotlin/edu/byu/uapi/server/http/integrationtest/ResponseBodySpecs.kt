package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.expectHeaderWithValue
import edu.byu.uapi.server.http.integrationtest.dsl.expectJsonBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.expectStatus
import edu.byu.uapi.server.http.integrationtest.dsl.expectTextBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.forAllMethodsIt
import edu.byu.uapi.server.http.integrationtest.dsl.hash
import edu.byu.uapi.server.http.integrationtest.dsl.request
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

/**
 * Makes sure that engines map the HTTP response body returned by the UAPI code to their own response properly.
 */
object ResponseBodySpecs : ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        forAllMethodsIt("handles text responses properly") { method ->
            givenRoutes {
                get { TestResponse.Text("foobar") }
                put { TestResponse.Text("foobar") }
                post { TestResponse.Text("foobar") }
                patch { TestResponse.Text("foobar") }
                delete { TestResponse.Text("foobar") }
            }
            whenCalledWith { request(method, "") }
            then {
                expectTextBodyEquals("foobar")
            }
        }
        forAllMethodsIt("handles JSON properly") { method ->
            val body = JsonTestBody(
                str = method.name,
                i = method.ordinal,
                o = JsonTestBody.Inner(bool = method.mayHaveBody)
            )
            givenRoutes {
                get { TestResponse.Json(body) }
                put { TestResponse.Json(body) }
                post { TestResponse.Json(body) }
                patch { TestResponse.Json(body) }
                delete { TestResponse.Json(body) }
            }
            whenCalledWith { request(method, "") }
            then {
                expectJsonBodyEquals(
                    """
                      {
                        str: "${method.name}",
                        i: ${method.ordinal},
                        o: {
                          bool: ${method.mayHaveBody}
                        }
                      }
                    """.trimIndent()
                )
            }
        }
        forAllMethodsIt("handles binary properly") { method ->
            givenRoutes {
                get { TestResponse.Body(binaryData, "some/binary") }
                put { TestResponse.Body(binaryData, "some/binary") }
                patch { TestResponse.Body(binaryData, "some/binary") }
                post { TestResponse.Body(binaryData, "some/binary") }
                delete { TestResponse.Body(binaryData, "some/binary") }
            }
            whenCalledWith { request(method, "") }
            then {
                expectStatus(200)
                expectHeaderWithValue("Content-Type", "some/binary")
                val bodyBytes = body().toByteArray()
                assertAll("Response body",
                    { assertEquals(binaryData.size, bodyBytes.size) },
                    { assertEquals(binaryData.hash(), bodyBytes.hash())}
                )
            }
        }
    }
}

private data class JsonTestBody(
    val str: String,
    val i: Int,
    val o: Inner
) {
    data class Inner(
        val bool: Boolean
    )
}
