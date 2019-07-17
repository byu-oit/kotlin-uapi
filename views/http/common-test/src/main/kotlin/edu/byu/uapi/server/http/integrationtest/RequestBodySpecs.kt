package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.expectTextBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.post

/**
 * The HTTP implementations should provide the handler with a representation of a body. The primary
 * way for the handlers to interact with the body is using HttpRequestBody#
 */
object RequestBodySpecs : ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        it("handles empty bodies") {
            givenRoutes {
                post {
                    //consumeBody doesn't invoke the
                    var invoked = false
                    val body = this.consumeBody { _, _ ->
                        invoked = true
                        "failure!"
                    }
                    TestResponse.Text("$invoked - $body")
                }
            }
            whenCalledWith { post("") }
            then {
                expectTextBodyEquals("false - null")
            }
        }
        describe("real bodies") {
            
        }
    }
}

