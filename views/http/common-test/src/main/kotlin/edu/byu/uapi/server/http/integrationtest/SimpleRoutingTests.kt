package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.HTTP_NO_CONTENT
import edu.byu.uapi.server.http.HTTP_OK
import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.delete
import edu.byu.uapi.server.http.integrationtest.dsl.emptyGet
import edu.byu.uapi.server.http.integrationtest.dsl.expectEmptyBody
import edu.byu.uapi.server.http.integrationtest.dsl.expectReceivedRequestLike
import edu.byu.uapi.server.http.integrationtest.dsl.expectStatus
import edu.byu.uapi.server.http.integrationtest.dsl.expectTextBody
import edu.byu.uapi.server.http.integrationtest.dsl.forAllMethodsIt
import edu.byu.uapi.server.http.integrationtest.dsl.get
import edu.byu.uapi.server.http.integrationtest.dsl.patch
import edu.byu.uapi.server.http.integrationtest.dsl.path
import edu.byu.uapi.server.http.integrationtest.dsl.pathParam
import edu.byu.uapi.server.http.integrationtest.dsl.pathSpec
import edu.byu.uapi.server.http.integrationtest.dsl.post
import edu.byu.uapi.server.http.integrationtest.dsl.put
import edu.byu.uapi.server.http.integrationtest.dsl.request
import edu.byu.uapi.server.http.integrationtest.dsl.suite
import kotlin.test.assertEquals

internal fun simpleRoutingTests() =
    suite("Simple Routing Tests") {
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
                expectTextBody(testMethod.name)
                expectReceivedRequestLike {
                    assertEquals(testMethod, method)
                    assertEquals("", path)
                }

            }
        }

        describe("path parameters") {
            givenRoutes {
                path("shared") {
                    emptyGet()
                }
            }
            it("should parse values for single parameter values") {
                givenRoutes {
                    pathSpec("/{one},{two}/{three}") {}
                    pathParam("one") {
                        pathParam("two") {
                            emptyGet()
                        }
                    }
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
                givenRoutes {
                    pathParam("one", "two") {
                        emptyGet()
                    }
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
                givenRoutes {
                    pathParam("outer") {
                        pathParam("one", "two", "three") {
                            pathParam("inner") {
                                emptyGet()
                            }
                        }
                    }
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
