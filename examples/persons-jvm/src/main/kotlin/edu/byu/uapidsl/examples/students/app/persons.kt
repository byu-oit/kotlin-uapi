package edu.byu.uapidsl.examples.students.app

import edu.byu.uapidsl.dsl.CollectionWithTotal
import edu.byu.uapidsl.dsl.PagingParams
import edu.byu.uapidsl.examples.students.dto.*
import edu.byu.uapidsl.http.NotFoundException


fun createPerson(command: CreatePerson, creatorByuId: String): String {
    val name = Name(
        command.firstName,
        command.middleName,
        command.surname,
        command.nameSuffix,
        command.preferredFirstName,
        command.preferredSurname
    )

    val netId = command.netId

    val byuId = Database.createPerson(name, netId)

    val created = Database.findPerson(byuId)!!

    val updated = created.copy(
        sex = command.sex,
        highSchoolCode =  HighSchools.find(command.highSchoolCode),
        homeCountryCode = Countries.find(command.homeCountryCode),
        homeStateCode = States.find(command.homeStateCode),
        homeTown = command.homeTown,
        restricted = command.restricted

    )

    Database.savePerson(updated)

    return byuId
}

fun updatePerson(byuId: String, command: UpdatePerson) {
    val p = Database.findPerson(byuId) ?: throw NotFoundException("person", byuId)


}

fun queryPeople(
    filters: PersonFilters,
    paging: PagingParams,
    canSeeRestrictedRecords: Boolean
): CollectionWithTotal<String> {
    TODO()
}

fun loadPerson(id: String, canSeeRestrictedRecords: Boolean): PersonDTO? {
    TODO("not implemented")
}

fun listPersonAddressTypes(byuId: String): Set<AddressType> {
    TODO()
}

fun getPersonAddress(byuId: String, type: AddressType): PersonAddressDTO? {
    TODO()
}
