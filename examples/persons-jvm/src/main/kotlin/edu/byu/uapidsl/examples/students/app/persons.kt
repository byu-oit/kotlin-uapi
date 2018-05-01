package edu.byu.uapidsl.examples.students.app

import edu.byu.uapidsl.dsl.CollectionWithTotal
import edu.byu.uapidsl.dsl.PagingParams
import edu.byu.uapidsl.examples.students.dto.*


fun createPerson(command: CreatePerson, creatorByuId: String) = "createdId"

fun queryPeople(
    filters: PersonListFilters,
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
