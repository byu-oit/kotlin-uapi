package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSuite
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSuiteInit

object ContentNegotiationSpecs: ComplianceSuite() {
    override fun ComplianceSuiteInit.define() {
        disabled = true
    }
}
