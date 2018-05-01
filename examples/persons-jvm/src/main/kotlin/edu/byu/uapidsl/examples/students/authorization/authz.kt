package edu.byu.uapidsl.examples.students.authorization

import edu.byu.jwt.ByuJwt

class Authorizer(val authJwt: ByuJwt) {

    val byuId = "person BYU ID"

    fun canCreatePerson() = true

    fun canSeeRestrictedRecords() = false

    fun canModifyPerson(targetByuId: String): Boolean {
        TODO()
    }

    fun canDeletePerson(targetPersonId: String): Boolean {
        TODO("not implemented")
    }

    fun canSeeCredentialsFor(byuId: String): Boolean {
        TODO("not implemented")
    }

    fun canSeeEmployeeInfo(byuId: String): Boolean {
        TODO("not implemented")
    }

    fun canSeePerson(h:
                     String): Boolean {
        TODO()
    }
}
