package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl

object RequestBodySpecs: ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        disabled = true
    }
}

