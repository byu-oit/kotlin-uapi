package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSuiteInit

object ResponseHeaderSpecs: ComplianceSpecSuite() {
    override fun ComplianceSuiteInit.define() {
        disabled = true
    }
}
