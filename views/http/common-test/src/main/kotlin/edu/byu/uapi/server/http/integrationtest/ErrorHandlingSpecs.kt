package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.it

/**
 * These tests ensure that the HTTP engines invoke the provided error mapper to wrap errors in a UAPI-style error
 * body.
 */
object ErrorHandlingSpecs: ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        it("Calls the error mapper when errors are thrown")
    }
}
