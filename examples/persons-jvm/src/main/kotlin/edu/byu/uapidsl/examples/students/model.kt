package edu.byu.uapidsl.examples.students

import edu.byu.uapidsl.apiModel
import edu.byu.uapidsl.dsl.uapiKey
import edu.byu.uapidsl.dsl.uapiProp
import edu.byu.uapidsl.examples.students.app.createPerson
import edu.byu.uapidsl.examples.students.app.getPersonAddress
import edu.byu.uapidsl.examples.students.app.loadPerson
import edu.byu.uapidsl.examples.students.app.queryPeople
import edu.byu.uapidsl.examples.students.authorization.Authorizer
import edu.byu.uapidsl.examples.students.dto.*
import edu.byu.uapidsl.types.ApiType

val personsModel = apiModel<Authorizer> {

    info {
        name = "Persons-JVM"
        version = "1.0.0"
        description = "Persons V2 mock implementation for Kotlin-JVM"
    }

    authContext {
        Authorizer(jwt)
    }

    resource<String, PersonDTO>("persons") {
        operations {
            read {
                authorized { authContext.canSeePerson(id) }
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
                authorized { authContext.canCreatePerson() }

//                possible {
//
//                }
//
//                validateInput {
//                    validate.isNotEmpty(input::name)
//                    validate.matches("""""".toRegex(), input::name)
//                }

                handle {
                    createPerson(input, authContext.byuId)
                }
            }

            update<UpdatePerson> {
                authorized {
                    authContext.canModifyPerson(id)
                }

//                possible {
//
//                }
//
//                validateInput {
//
//                }

                handle {
                    TODO()
                }

            }

            delete {
                authorized { authContext.canDeletePerson(resource.personId) }

//                possible {
//
//                }

                handle {
                    TODO()
                }
            }

        }

        output<UAPIPerson> {

            example = UAPIPerson("pid", "byuId", "name")

            transform {
                UAPIPerson(resource)
            }

        }

        subresources {

            collection<AddressType, PersonAddressDTO>("addresses") {

                operations {
                    listSimple<Unit> {
                        listIds {
                            AddressType.values().toList()
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

            collection<PersonCredentialId, PersonCredentialDTO>("credentials") {
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

            collection<EmailType, PersonEmailDTO>("email_addresses") {
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

            single<EmployeeSummary>("employee_summaries") {
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

    extend {

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

class UAPIPerson(personId: String, byuId: String, name: String) {

    constructor(dto: PersonDTO): this("pid", dto.byuId, "name")

    val personId = uapiProp(
        value = personId,
        apiType = ApiType.SYSTEM
    )

    val byuId = uapiKey(
        value = byuId,
        apiType = ApiType.SYSTEM
    )

    val name = uapiProp(
        value = name
    )

}

