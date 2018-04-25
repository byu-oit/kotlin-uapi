package edu.byu.uapidsl.examples.students.dto

class PersonDTO {
    val personId = "pid"
    val byuId = "byuId"
}

class CreatePerson

class UpdatePerson

data class PersonListFilters(
        val byuId: Set<String>,
        val personId: Set<String>,
        val netId: Set<String>,
        val credentialType: CredentialType?,
        val userName: Set<String>,
        val ssn: Set<String>,
        val emailAddress: String?,
        val emailAlias: String?,
        val phoneNumber: Set<String>,
        val surname: String?
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

enum class AddressType {
    WRK, RES
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
