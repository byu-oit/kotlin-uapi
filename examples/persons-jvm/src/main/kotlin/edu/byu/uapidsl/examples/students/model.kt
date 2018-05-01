package edu.byu.uapidsl.examples.students

import edu.byu.uapidsl.apiModel
import edu.byu.uapidsl.dsl.uapiProp
import edu.byu.uapidsl.examples.students.app.createPerson
import edu.byu.uapidsl.examples.students.app.getPersonAddress
import edu.byu.uapidsl.examples.students.app.loadPerson
import edu.byu.uapidsl.examples.students.app.queryPeople
import edu.byu.uapidsl.examples.students.authorization.Authorizer
import edu.byu.uapidsl.examples.students.dto.*
import edu.byu.uapidsl.types.ApiType

val personsModel = apiModel<Authorizer> {

    authContext {
        Authorizer(jwt)
    }

    resource<String, PersonDTO>("persons") {
        operations {
            read {
                authorization { authContext.canSeePerson(id) }
                handle {
                    loadPerson(id, authContext.canSeeRestrictedRecords())
                }
            }

            listPaged<PersonListFilters> {
                defaultSize = 50
                maxSize = 200
                listIds {
                    queryPeople(filters, paging, authContext.canSeeRestrictedRecords())
                }
            }

            create<CreatePerson> {
                authorization { authContext.canCreatePerson() }
                handle {
                    createPerson(input, authContext.byuId)
                }
            }

            update<UpdatePerson> {
                authorization { authContext.canModifyPerson(id) }
                handle {
                    TODO()
                }

            }

            delete {
                authorization { authContext.canDeletePerson(resource.personId) }
                handle {
                    TODO()
                }
            }

        }

        model {

            example = PersonDTO()

            transform<UAPIPerson> {
                UAPIPerson(resource, authContext)
            }

            collectionSubresource<AddressType, PersonAddressDTO>("addresses") {

                operations {
                    listSimple<Unit> {
                        listIds {
                            TODO()
                        }
                    }
                    read {
                        handle {
                            getPersonAddress(parentId, id)
                        }
                    }
                    createOrUpdate<Any> {
                        authorization { true }
                        handle {
                        }
                    }
                }

                model {
                }
            }

            collectionSubresource<PersonCredentialId, PersonCredentialDTO>("credentials") {
                authorization { authContext.canSeeCredentialsFor(parentId) }

                operations {
                    listPaged<Unit> {
                        maxSize = 20
                        defaultSize = 20

                        listObjects {
                            TODO("Not Implemented")
                        }
                    }
                    read {
                        handle {
                            TODO()
                        }
                    }

                }
            }

            collectionSubresource<EmailType, PersonEmailDTO>("email_addresses") {
                operations {
                    read {
                        handle {
                            TODO()
                        }
                    }

                    listSimple<Unit> {
                        listIds { TODO() }
                    }
                }
            }

            singleSubresource<EmployeeSummary>("employee_summaries") {
                operations {
                    read {
                        authorization { authContext.canSeeEmployeeInfo(parentId) }

                        handle {
                            TODO()
                        }
                    }
                }
            }
        }
    }

//  domain<StateCode> {
//    list {
//      getStateCodes()
//    }
//
//    id {
//      code.id
//    }
//  }

}

class UAPIPerson(person: PersonDTO, authContext: Authorizer) {
    val byuId = uapiProp(
        value = person.byuId,
        apiType = byuIdApiType(person.byuId, authContext)
    )

}

fun byuIdApiType(value: String, authContext: Authorizer) =
    if (authContext.canModifyPerson(value)) ApiType.MODIFIABLE
    else ApiType.READ_ONLY



