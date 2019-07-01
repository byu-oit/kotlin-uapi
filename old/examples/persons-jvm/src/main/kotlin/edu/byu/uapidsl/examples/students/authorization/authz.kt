package edu.byu.uapidsl.examples.students.authorization

class Authorizer(val byuId: String) {

    fun canCreatePerson() = true

    fun canSeeRestrictedRecords() = false

    fun canModifyPerson(targetByuId: String) = true

    fun canDeletePerson(targetPersonId: String) = true

    fun canSeeCredentialsFor(byuId: String) = true

    fun canSeeEmployeeInfo(byuId: String) = true

    fun canSeePerson(h:
                     String): Boolean {
        return true
    }

    fun isRegistrar(): Boolean {
        return false
    }
}
