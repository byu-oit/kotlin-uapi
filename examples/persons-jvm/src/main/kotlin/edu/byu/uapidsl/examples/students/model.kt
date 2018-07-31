package edu.byu.uapidsl.examples.students

import edu.byu.uapidsl.apiModel
import edu.byu.uapidsl.examples.students.app.*
import edu.byu.uapidsl.examples.students.authorization.Authorizer
import edu.byu.uapidsl.examples.students.dto.*
import edu.byu.uapidsl.examples.students.dto.EmailType
import edu.byu.uapidsl.examples.students.dto.EmployeeSummary
import edu.byu.uapidsl.model.validation.expectNotBlank

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

        example = joe.person.toDTO(Authorizer(""))

//        output<UAPIPerson> {
//            transform {
//                UAPIPerson(resource)
//            }
//        }

        idFromModel { it.byuId.value }

        isRestricted { resource.restricted.value }

        operations {
            read {
                authorized { authContext.canSeePerson(resource.id) }
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

//            listSimple<PersonFilters> {
//                listIds {
//                    Database.searchPeople(filters)
//                }
//            }

            create<CreatePerson> {
                authorized { authContext.canCreatePerson() }

//                possible {
//
//                }
//
//                validateInput {
//                    validateInput.isNotEmpty(input::name)
//                    validateInput.matches("""""".toRegex(), input::name)
//                }

                handle {
                    createPerson(input, authContext.byuId)
                }
            }

            update<UpdatePerson> {
                authorized {
                    authContext.canModifyPerson(resource.id)
                }

                validateInput {
                    if (input.firstName != null) {
                        expectNotBlank(input::firstName)
                    }
                }

                handle {
                    val p = Database.findPerson(resource.id)!!
                    val surname = input.surname
                    if (surname != null) {
                        p.name.surname = surname
                    }
                }

            }

            delete {
                authorized { authContext.canDeletePerson(resource.model.personId.value) }

//                possible {
//
//                }

                handle {
                    Database.deletePerson(resource.id)
                }
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

    /*
    singletonResource<PersonAPISettings>("settings") {
        operations {
            read {
                authorized {}
                handle {}
            }
            update {
                authorized {}
                handle {}
            }
        }

        subresources {

        }
    }
     */

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
