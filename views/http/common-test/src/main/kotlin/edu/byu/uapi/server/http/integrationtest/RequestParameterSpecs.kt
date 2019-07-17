package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.emptyGet
import edu.byu.uapi.server.http.integrationtest.dsl.expectReceivedRequestLike
import edu.byu.uapi.server.http.integrationtest.dsl.expectTextBody
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests rules about how query parameters and headers are handled. Path parameters are tested as part of
 * [SimpleRoutingSpecs].
 */
object RequestParameterSpecs : ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        givenRoutes {
            emptyGet()
        }
        describe("query params") {
            it("passes query parameters to the handler") {
                whenCalledWith { get("", listOf("foo" to "bar", "bar" to "baz")) }
                then {
                    expectReceivedRequestLike {
                        assertEquals(
                            mapOf(
                                "foo" to listOf("bar"),
                                "bar" to listOf("baz")
                            ),
                            queryParams
                        )
                    }
                }
            }
            it("handles duplicated query parameters") {
                whenCalledWith { get("", listOf("foo" to "bar", "foo" to "baz")) }
                then {
                    expectReceivedRequestLike {
                        assertEquals(
                            mapOf(
                                "foo" to listOf("bar", "baz")
                            ),
                            queryParams
                        )
                    }
                }
            }
        }
        describe("headers") {
            it("passes headers to the handler") {
                whenCalledWith { get("").header("foo" to "bar", "bar" to "baz") }
                then {
                    expectReceivedRequestLike {
                        assertEquals("bar", headers["foo"])
                        assertEquals("baz", headers["bar"])
                    }
                }
            }
            it("normalizes header names to lower case") {
                givenRoutes("/echoNames") {
                    get {
                        TestResponse.Text(
                            headers.keys.joinToString("\n")
                        )
                    }
                }
                whenCalledWith { get("/echoNames").header("FOO" to "bar", "BaR" to "baz") }
                then {
                    val bodyLines = expectTextBody().lines()
                    assertAll(
                        { assertTrue("foo" in bodyLines) },
                        { assertTrue("bar" in bodyLines) }
                    )
                    assertAll(bodyLines.map { l -> { assertEquals(l.toLowerCase(), l) } })
                }
            }
        }
    }
}
