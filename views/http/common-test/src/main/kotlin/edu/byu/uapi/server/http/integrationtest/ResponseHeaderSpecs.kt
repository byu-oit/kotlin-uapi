package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.expectHeaderWithValue
import edu.byu.uapi.server.http.integrationtest.dsl.get

object ResponseHeaderSpecs: ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        givenRoutes {
            get {
                TestResponse.Empty(headers = mapOf(
                    "foo" to "bar",
                    "BAZ" to "zab"
                ))
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
}
