package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.HTTP_NO_CONTENT
import edu.byu.uapi.server.http.HTTP_OK
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.emptyGet
import edu.byu.uapi.server.http.integrationtest.dsl.expectEmptyBody
import edu.byu.uapi.server.http.integrationtest.dsl.expectReceivedRequestLike
import edu.byu.uapi.server.http.integrationtest.dsl.expectStatus
import edu.byu.uapi.server.http.integrationtest.dsl.expectTextBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.forAllMethodsIt
import kotlin.test.assertEquals

/**
 * Ensures that paths and methods are routed properly. Doesn't test content negotiation; see [ContentNegotiationSpecs].
 */
object SimpleRoutingSpecs : ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        forAllMethodsIt("should route to the method's handler") { testMethod ->
            givenRoutes {
                get { TestResponse.Text("GET") }
                put { TestResponse.Text("PUT") }
                post { TestResponse.Text("POST") }
                patch { TestResponse.Text("PATCH") }
                delete { TestResponse.Text("DELETE") }
            }
            whenCalledWith { request(testMethod) }
            then {
                expectStatus(HTTP_OK)
                expectTextBodyEquals(testMethod.name)
                expectReceivedRequestLike {
                    assertEquals(testMethod.name, method)
                    assertEquals("", path)
                }

            }
        }

        describe("pathSpec parameters") {
            it("should parse values for single parameter values") {
                givenRoutes("/{one}/{two}") {
                    emptyGet()
                }
                whenCalledWith { get("/abcdef/123") }
                then {
                    expectStatus(HTTP_NO_CONTENT)
                    expectEmptyBody()
                    expectReceivedRequestLike {
                        assertEquals("GET", method)
                        assertEquals(mapOf("one" to "abcdef", "two" to "123"), pathParams)
                    }
                }
            }
            it("compound params") {
                givenRoutes("/{one},{two}") {
                    emptyGet()
                }
                whenCalledWith { get("/abcdef,123") }
                then {
                    expectStatus(HTTP_NO_CONTENT)
                    expectEmptyBody()
                    expectReceivedRequestLike {
                        assertEquals("GET", method)
                        assertEquals(mapOf("one" to "abcdef", "two" to "123"), pathParams)
                    }
                }
            }
            it("mixed param types") {
                givenRoutes("/{outer}/{one},{two},{three}/{inner}") {
                    emptyGet()
                }
                whenCalledWith { get("/a/b,c,d/e") }
                then {
                    expectStatus(HTTP_NO_CONTENT)
                    expectEmptyBody()
                    expectReceivedRequestLike {
                        assertEquals("GET", method)
                        assertEquals(
                            mapOf(
                                "outer" to "a",
                                "one" to "b",
                                "two" to "c",
                                "three" to "d",
                                "inner" to "e"
                            ), pathParams
                        )
                    }
                }
            }
        }
    }
}
