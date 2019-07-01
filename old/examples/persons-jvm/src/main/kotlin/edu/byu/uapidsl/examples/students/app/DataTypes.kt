package edu.byu.uapidsl.examples.students.app

import java.time.LocalDate

data class Person(
    val personId: String,
    val byuId: String,
    val netId: String? = null,

    val name: Name,
    val sex: Sex = Sex.UNKNOWN,

    val deceased: Boolean = false,

    val highSchoolCode: HighSchoolCode? = null,

    val homeCountryCode: CountryCode? = null,
    val homeStateCode: StateCode? = null,
    val homeTown: String? = null,

    val mergeInProcess: Boolean = false,

//    val personalEmail: String? = null,
//    val primaryPhone: String? = null,

    val restricted: Boolean = false//,
//    val update: UpdateInfo
)

enum class Sex {
    MALE,
    FEMALE,
    UNKNOWN
}

data class Name(
    var first: String,
    var middle: String? = null,
    var surname: String,
    var suffix: String? = null,
    var preferredFirst: String? = null,
    var preferredSurname: String? = null
) {

    val preferred: String
        get() = derivePreferred(first, surname, preferredFirst, preferredSurname)

    val restOfName = if (middle != null) {
        "$first $middle"
    } else {
        first
    }

    val firstNameFirst
        get() = arrayOf(first, middle, surname, suffix)
            .filterNotNull()
            .joinToString(" ")

    val lastNameFirst
        get() = arrayOf(surname, restOfName, suffix)
            .filterNotNull()
            .joinToString(", ")
}

internal fun derivePreferred(first: String, surname: String, preferredFirst: String?, preferredSurname: String?): String {
    val f = preferredFirst ?: first
    val l = preferredSurname ?: surname

    return "$f $l"
}

data class HighSchoolCode(
    val id: String,
    val name: String,
    val city: String,
    val state: StateCode
)

data class StateCode(
    val id: String,
    val commonName: String,
    val fullName: String = commonName,
    val country: CountryCode
)

data class CountryCode(
    val id: String,
    val commonName: String,
    val fullName: String = commonName,
    val callingCode: String
)

enum class AddressType(
    val serialized: String
) {
    RESIDENTIAL("RES"),
    MAILING("MAL"),
    WORK("WRK"),
    PERMANENT("PRM")
}

data class Address(
    val type: AddressType,
    var lines: List<String>,
    var building: Building? = null,
    var city: String,
    var stateCode: StateCode,
    var countryCode: CountryCode,

    var postalCode: String?,
    var unlisted: Boolean = false,
    var verified: Boolean = false//,
//    val update: UpdateInfo
)

data class Building(
    val code: String,
    val name: String
)

enum class EmailType {
    PERSONAL, SECURITY, WORK
}

data class EmailAddress(
    val type: EmailType,
    val value: String,

    val unlisted: Boolean = false,
    val verified: Boolean = false//,
//    val update: UpdateInfo
)

//data class UpdateInfo(
//    val createdById: String,
//    val dateTimeCreated: Instant,
//    val updatedById: String,
//    val dateTimeUpdated: Instant
//)

data class EmployeeSummary(
    var department: String,
    var employeeType: String,
    var hireDate: LocalDate,
    var jobTitle: String,
    var reportsToId: String?
)

data class PhoneNumber(

    val value: String,
    var cell: Boolean = false,
    var countryCode: CountryCode,

    var primary: Boolean = false,
    var textsOkay: Boolean = cell,
    var tty: Boolean = false,
    var unlisted: Boolean = false,
    var work: Boolean = false//,
//    val update: UpdateInfo
) {
    val lookup: String = value.trim()
        .replace("\\D".toRegex(), "")
}
