package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.integrationtest.dsl.suite
import kotlin.test.assertEquals

internal val simpleRoutingTests =
    suite("Simple Routing Tests") {
        routes {
            path("simple") {
                echoGet()
                echoPost()
                echoPut()
                echoPatch()
                echoDelete()
            }

            path("params") {
                path("single") {
                    pathParam("one") {
                        pathParam("two") {
                            echoGet()
                        }
                    }
                }
                path("compound") {
                    pathParam("one", "two") {
                        echoGet()
                    }
                }
            }
        }

        tests {
            group("simple") {
                HttpMethod.Routable.values().forEach { testMethod ->
                    test(testMethod.name) {
                        request(testMethod, "/simple") {}
                        should {
                            assertStatus(200)
                            assertEchoed {
                                assertEquals(testMethod.name, method)
                                assertEquals("/simple", path)
                            }
                        }
                    }
                }
            }

            group("path parameters") {
                test("single params") {
                    get("/params/single/abcdef/123") {}
                    should {
                        assertStatus(200)
                        assertEchoed {
                            assertEquals("GET", method)
                            assertEquals(mapOf("one" to "abcdef", "two" to "123"), pathParams)
                        }
                    }
                }
                test("compound params") {
                    get("/params/compound/abcdef,123") {}
                    should {
                        assertStatus(200)
                        assertEchoed {
                            assertEquals("GET", method)
                            assertEquals(mapOf("one" to "abcdef", "two" to "123"), pathParams)
                        }
                    }
                }
            }
        }
    }
