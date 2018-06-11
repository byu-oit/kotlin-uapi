package edu.byu.uapidsl.examples.students.dto

import edu.byu.uapidsl.dsl.uapiKey
import edu.byu.uapidsl.dsl.uapiProp
import edu.byu.uapidsl.examples.students.app.AddressType
import edu.byu.uapidsl.examples.students.app.Person
import edu.byu.uapidsl.examples.students.app.Sex
import edu.byu.uapidsl.examples.students.authorization.Authorizer
import edu.byu.uapidsl.types.ApiType
import edu.byu.uapidsl.types.UAPIField

class PersonDTO(person: Person, canModifyPersonalInfo: Boolean) {

    private val personalInfoType = if (canModifyPersonalInfo) ApiType.MODIFIABLE else ApiType.READ_ONLY

    val personId = uapiProp(
        value = person.personId,
        apiType = ApiType.SYSTEM
    )

    val byuId = uapiKey(
        value = person.byuId,
        apiType = ApiType.SYSTEM
    )

    val netId = uapiProp(
        value = person.netId,
        apiType = ApiType.RELATED
    )

    val firstName = uapiProp(person.name.first, personalInfoType)
    val middleName = uapiProp(person.name.middle, personalInfoType)
    val surname = uapiProp(person.name.surname, personalInfoType)
    val suffix = uapiProp(person.name.suffix, personalInfoType)
    val preferredFirstName = uapiProp(person.name.preferredFirst, ApiType.MODIFIABLE)
    val preferredSurname = uapiProp(person.name.preferredSurname, ApiType.MODIFIABLE)

    val restOfName = uapiProp(person.name.restOfName, ApiType.DERIVED)
    val nameLnf = uapiProp(person.name.lastNameFirst, ApiType.DERIVED)
    val nameFnf = uapiProp(person.name.firstNameFirst, ApiType.DERIVED)
    val preferredName = uapiProp(person.name.preferred, ApiType.DERIVED)

    val deceased = uapiProp(person.deceased, ApiType.RELATED)

    val homeTown = uapiProp(person.homeTown, ApiType.MODIFIABLE)
    val homeCountryCode: UAPIField<String?> = person.homeCountryCode.run {
        uapiProp(
            value = this?.id,
            apiType = ApiType.MODIFIABLE,
            description = this?.commonName,
            longDescription = this?.fullName
        )
    }

    val highSchoolCode: UAPIField<String?> = person.highSchoolCode.run {
        uapiProp(
            value = this?.id,
            apiType = ApiType.MODIFIABLE,
            description = this?.name
        )
    }

    val highSchoolCity = uapiProp(
        value = person.highSchoolCode?.city,
        apiType = ApiType.RELATED
    )

    val highSchoolStateCode = person.highSchoolCode?.state.run {
        uapiProp(
            value = this?.id,
            apiType = ApiType.RELATED,
            description = this?.commonName,
            longDescription = this?.fullName
        )
    }

    val restricted = uapiProp(
        value = person.restricted,
        apiType = ApiType.MODIFIABLE
    )

    val mergeInProcess = uapiProp(person.mergeInProcess, ApiType.DERIVED)

//    val personalEmailAddress = uapiProp()
//    val primaryPhoneNumber: uapiProp()

    val sex = uapiProp(person.sex, personalInfoType)

}

fun Person.toDTO(authz: Authorizer): PersonDTO {
    return PersonDTO(this, authz.isRegistrar())
}

data class CreatePerson(
    val firstName: String,
    val surname: String,
    val middleName: String? = null,
    val nameSuffix: String? = null,
    val preferredFirstName: String? = null,
    val preferredSurname: String? = null,
    val netId: String? = null,
    val sex: Sex = Sex.UNKNOWN,
    val highSchoolCode: String? = null,
    val homeCountryCode: String? = null,
    val homeStateCode: String? = null,
    val homeTown: String? = null,
    val restricted: Boolean = false
)

data class UpdatePerson(
    val firstName: String? = null,
    val surname: String? = null,
    val middleName: String? = null,
    val nameSuffix: String? = null,
    val preferredFirstName: String? = null,
    val preferredSurname: String? = null,
    val sex: Sex? = null,
    val highSchoolCode: String? = null,
    val homeCountryCode: String? = null,
    val homeStateCode: String? = null,
    val homeTown: String? = null,
    val restricted: Boolean? = null
)

data class PersonFilters(
    val byuId: Set<String> = emptySet(),
    val personId: Set<String> = emptySet(),
    val netId: Set<String> = emptySet(),
    val credentialType: CredentialType? = null,
    val userName: Set<String> = emptySet(),
    val ssn: Set<String> = emptySet(),
    val emailAddress: String? = null,
    val emailAlias: String? = null,
    val phoneNumber: Set<String> = emptySet(),
    val surname: String? = null,
    val addresses: AddressFilters? = null
)

data class AddressFilters(
    val addressType: AddressType? = null,
    val zipCode: Set<String> = emptySet()
)

enum class CredentialType {
    BYU_HAWAII_ID,
    BYU_IDAHO_ID,
    FACEBOOK_ID,
    GOOGLE_ID,
    ID_CARD,
    LDS_ACCOUNT_ID,
    LDS_CMIS_ID,
    NET_ID,
    PROX_CARD,
    SEMINARY_STUDENT_ID,
    SEOS_CARD,
    WSO2_CLIENT_ID
}

class PersonAddressDTO

class PersonEmailDTO

class PersonRelationshipDTO

data class PersonCredentialId(
    val credentialType: CredentialType,
    val credentialId: String
)

class PersonCredentialDTO


enum class EmailType {
    WRK, RES
}

class EmployeeSummary
