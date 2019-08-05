package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.describe
import edu.byu.uapi.server.http.integrationtest.dsl.expectHeaderWithValue

/**
 * Ensures that engines map HTTP response headers and statuses properly.
 */
object ResponseHeaderSpecs : ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        describe("headers") {
            givenRoutes {
                get {
                    TestResponse.Empty(
                        headers = mapOf(
                            "foo" to "bar",
                            "BAZ" to "zab"
                        )
                    )
                }
            }
            it("Should return the exact headers and values specified") {
                whenCalledWith { get("") }
                then {
                    expectHeaderWithValue("foo", "bar")
                    expectHeaderWithValue("BAZ", "zab")
                }
            }
        }
        describe("statuses")
    }
}
