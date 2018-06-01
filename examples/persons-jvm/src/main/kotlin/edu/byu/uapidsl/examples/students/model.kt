package edu.byu.uapidsl.examples.students

import edu.byu.uapidsl.apiModel
import edu.byu.uapidsl.examples.students.app.*
import edu.byu.uapidsl.examples.students.authorization.Authorizer
import edu.byu.uapidsl.examples.students.dto.*
import edu.byu.uapidsl.examples.students.dto.EmailType
import edu.byu.uapidsl.examples.students.dto.EmployeeSummary

val personsModel = apiModel<Authorizer> {

    info {
        name = "Persons-JVM"
        version = "1.0.0"
        description = "Persons V2 mock implementation for Kotlin-JVM"
    }

    authContext {
//        Authorizer(if (jwt.hasResourceOwner()) jwt.resourceOwnerClaims!!.byuId!! else jwt.clientClaims.byuId!!)
        Authorizer("fake")
    }

    resource<String, PersonDTO>("persons") {
        operations {
            read {
                authorized { authContext.canSeePerson(id) }
                handle {
                    println("Loading person for id $id")
                    Database.findPerson(id)?.toDTO(authContext)
                }
            }

            listPaged<PersonFilters> {
                defaultSize = 50
                maxSize = 200
                listIds {
                    Database.searchPeoplePaged(filters, paging)
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
                authorized { authContext.canDeletePerson(resource.personId.value) }

//                possible {
//
//                }

                handle {
                    Database.deletePerson(id)
                }
            }

        }

        example = joe.person.toDTO(Authorizer(""))

//        output<UAPIPerson> {
//
//
//            transform {
//                UAPIPerson(resource)
//            }
//
//        }

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

//                model {
//                }
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
