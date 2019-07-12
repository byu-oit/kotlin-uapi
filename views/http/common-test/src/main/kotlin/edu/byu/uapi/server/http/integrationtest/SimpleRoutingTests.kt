package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.integrationtest.dsl.describeAllMethods
import edu.byu.uapi.server.http.integrationtest.dsl.emptyDelete
import edu.byu.uapi.server.http.integrationtest.dsl.emptyGet
import edu.byu.uapi.server.http.integrationtest.dsl.emptyPatch
import edu.byu.uapi.server.http.integrationtest.dsl.emptyPost
import edu.byu.uapi.server.http.integrationtest.dsl.emptyPut
import edu.byu.uapi.server.http.integrationtest.dsl.request
import edu.byu.uapi.server.http.integrationtest.dsl.suite
import kotlin.test.assertEquals

internal val simpleRoutingTests =
    suite("Simple Routing Tests") {
        describeAllMethods("method routing") { testMethod ->
            givenRoutes {
                emptyGet()
                emptyPut()
                emptyPost()
                emptyPatch()
                emptyDelete()
            }
            whenCalledWith { request(testMethod, "") }
            then {
                assertStatus(204)
                assertEmptyBody()
                assertReceivedRequest {
                    assertEquals(testMethod, method)
                    assertEquals("", path)
                }

            }
        }

        describe("path parameters") {
            it("single params") {
                givenRoutes {
                    pathParam("one") {
                        pathParam("two") {
                            emptyGet()
                        }
                    }
                }
                whenCalledWith { get("/abcdef/123") }
                then {
                    assertStatus(204)
                    assertEmptyBody()
                    assertReceivedRequest {
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
                    assertStatus(204)
                    assertEmptyBody()
                    assertReceivedRequest {
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
                    assertStatus(204)
                    assertEmptyBody()
                    assertReceivedRequest {
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
