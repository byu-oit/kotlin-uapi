package edu.byu.uapi.server.http.integrationtest

import com.github.kittinunf.fuel.core.Request
import edu.byu.uapi.server.http.HTTP_OK
import edu.byu.uapi.server.http.HTTP_UNSUPPORTED_TYPE
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.expectBodyOfType
import edu.byu.uapi.server.http.integrationtest.dsl.expectJsonBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.expectStatus
import edu.byu.uapi.server.http.integrationtest.dsl.expectTextBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.get
import edu.byu.uapi.server.http.integrationtest.dsl.post

/**
 * This suite defines the expected behaviors for matching routes based on the media types the consume and produce.
 *
 * When consuming types, there is only one possible value in the `Content-Type`, so the implementation should match
 * the provided content/type header to the best-matching route.
 *
 * When getting a response, the `Accept` header can get quite complex. Ideally, the underlying HTTP server will
 * offer a way to give multiple routes with different types, including wildcards, and will handle matching them to
 * the accept header itself.
 */
object ContentNegotiationSpecs : ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        describe("request content negotiation (Content-Type:)") {
            givenRoutes {
                post(consumes = "foo/bar") { TestResponse.Text("foo/bar handler") }
                post(consumes = "foo/*") { TestResponse.Text("foo/* handler") }
                post(consumes = "bar/baz") { TestResponse.Text("bar/baz handler") }
                post { TestResponse.Text("default handler") }
            }
            it("picks the exact match") {
                whenCalledWith { post("").type("foo/bar").body("foobar") }
                then {
                    expectStatus(HTTP_OK)
                    expectTextBodyEquals("foo/bar handler")
                }
            }
            it("respects wildcards") {
                whenCalledWith { post("").type("foo/oof").body("foobar") }
                then {
                    expectStatus(HTTP_OK)
                    expectTextBodyEquals("foo/* handler")
                }
            }
            it("falls back to the default handler") {
                whenCalledWith { post("").type("oof/oof").body("foobar") }
                then {
                    expectStatus(HTTP_OK)
                    expectTextBodyEquals("default handler")
                }
            }
            it("fails with UAPI-style HTTP 415 if there is no default") {
                givenRoutes("no-default") {
                    post(consumes = "foo/bar") {
                        TestResponse.Text("foo/bar")
                    }
                    post(consumes = "foo/*") { TestResponse.Text("foo/*") }
                    post(consumes = "bar/baz") { TestResponse.Text("bar/baz") }
                }
                whenCalledWith { post("no-default").type("oof/oof").body("foobar") }
                then {
                    expectStatus(HTTP_UNSUPPORTED_TYPE)
                    @Suppress("MaxLineLength")
                    expectJsonBodyEquals(
                        """
                        {
                          "metadata": {
                            "validation_response": {
                              "code": 415,
                              "message": "Unsupported Media Type"
                            },
                            "validation_information": [
                              "Unable to process the provided Content-Type header. Acceptable content types are bar/baz, foo/*, foo/bar"
                            ]
                          }
                        }
                    """.trimIndent()
                    )
                }
            }
        }
        describe("response content negotiation (Accept:)") {
            givenRoutes {
                get(produces = "foo/bar") { TestResponse.Body("foo/bar handler", "foo/bar") }
                get(produces = "foo/*") { TestResponse.Body("foo/* handler", "foo/star") }
                get(produces = "bar/baz") { TestResponse.Body("bar/baz handler", "bar/baz") }
                get { TestResponse.Body("default handler", "star/star") }
            }
            describe("single value headers") {
                it("picks the exact match") {
                    whenCalledWith { get("").accept("foo/bar") }
                    then {
                        expectBodyOfType("foo/bar")
                    }
                }
                it("matches wildcards to wildcards") {
                    whenCalledWith { get("").accept("foo/*") }
                    then {
                        expectBodyOfType("foo/star")
                    }
                }
                it("matches wildcards to more specific routes") {
                    whenCalledWith { get("").accept("bar/*") }
                    then {
                        expectBodyOfType("bar/baz")
                    }
                }
                it("falls back to default if nothing matches") {
                    whenCalledWith { get("").accept("other/*") }
                    then {
                        expectBodyOfType("star/star")
                    }
                }
            }
            describe("complex Accept headers") {
                it("prefers higher-quality types") {
                    whenCalledWith { get("").accept("foo/bar, foo/*;q=0.4, */*;q=0") }
                    then {
                        expectBodyOfType("foo/bar")
                    }
                }
                it("Can fall through to lower-quality types") {
                    whenCalledWith { get("").accept("zab/zab, zab/*;q=0.6, foo/*;q=0.2, */*;q=0.1") }
                    then {
                        //I'm not sure if this is actually what we'd expect out of every provider. It's possible
                        // that they might pick the foo/bar route. For now, I'm going off of what Spark is doing,
                        // and we'll cross that bridge when we come to it.
                        expectBodyOfType("foo/star")
                    }
                }
                it("Falls through to default if nothing matches") {
                    whenCalledWith { get("").accept("zab/zab, zab/*;q=0.6, oof/*;q=0.2, */*;q=0.1") }
                    then {
                        expectBodyOfType("star/star")
                    }
                }
            }
        }
    }
}

private fun Request.type(type: String): Request {
    return header("Content-Type", type)
}

private fun Request.accept(type: String): Request {
    return header("Accept", type)
}
