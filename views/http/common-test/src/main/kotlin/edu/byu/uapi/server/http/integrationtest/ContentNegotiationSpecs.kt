package edu.byu.uapi.server.http.integrationtest

import com.github.kittinunf.fuel.core.Request
import edu.byu.uapi.server.http.HTTP_OK
import edu.byu.uapi.server.http.HTTP_UNSUPPORTED_MEDIA_TYPE
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.expectJsonBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.expectStatus
import edu.byu.uapi.server.http.integrationtest.dsl.expectTextBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.post

object ContentNegotiationSpecs : ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        describe("request content negotiation") {
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
                    expectStatus(HTTP_UNSUPPORTED_MEDIA_TYPE)
                    expectJsonBodyEquals("""
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
                    """.trimIndent())
                }
            }
        }
        describe("response content negotiation") {

        }
    }
}

private fun Request.type(type: String): Request {
    return header("Content-Type", type)
}

private fun Request.accept(type: String): Request {
    return header("Accepts", type)
}
