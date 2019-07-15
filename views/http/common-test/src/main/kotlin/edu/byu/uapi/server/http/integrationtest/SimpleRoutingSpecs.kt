package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.HTTP_NO_CONTENT
import edu.byu.uapi.server.http.HTTP_OK
import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSuite
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSuiteInit
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.delete
import edu.byu.uapi.server.http.integrationtest.dsl.emptyGet
import edu.byu.uapi.server.http.integrationtest.dsl.expectEmptyBody
import edu.byu.uapi.server.http.integrationtest.dsl.expectReceivedRequestLike
import edu.byu.uapi.server.http.integrationtest.dsl.expectStatus
import edu.byu.uapi.server.http.integrationtest.dsl.expectTextBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.forAllMethodsIt
import edu.byu.uapi.server.http.integrationtest.dsl.get
import edu.byu.uapi.server.http.integrationtest.dsl.patch
import edu.byu.uapi.server.http.integrationtest.dsl.post
import edu.byu.uapi.server.http.integrationtest.dsl.put
import edu.byu.uapi.server.http.integrationtest.dsl.request
import kotlin.test.assertEquals

object SimpleRoutingSpecs : ComplianceSuite() {
    override fun ComplianceSuiteInit.define() {
        forAllMethodsIt("should route to the method's handler") { testMethod ->
            givenRoutes {
                get { TestResponse.Text("GET") }
                put { TestResponse.Text("PUT") }
                post { TestResponse.Text("POST") }
                patch { TestResponse.Text("PATCH") }
                delete { TestResponse.Text("DELETE") }
            }
            whenCalledWith { request(testMethod, "") }
            then {
                expectStatus(HTTP_OK)
                expectTextBodyEquals(testMethod.name)
                expectReceivedRequestLike {
                    assertEquals(testMethod, method)
                    assertEquals("", path)
                }

            }
        }

        describe("path parameters") {
            it("should parse values for single parameter values") {
                givenRoutes("/{one}/{two}") {
                    emptyGet()
                }
                whenCalledWith { get("/abcdef/123") }
                then {
                    expectStatus(HTTP_NO_CONTENT)
                    expectEmptyBody()
                    expectReceivedRequestLike {
                        assertEquals(HttpMethod.GET, method)
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
                        assertEquals(HttpMethod.GET, method)
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
                        assertEquals(HttpMethod.GET, method)
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
