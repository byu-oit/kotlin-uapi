package edu.byu.uapidsl.examples.students

import edu.byu.uapidsl.apiModel
import edu.byu.uapidsl.examples.students.app.*
import edu.byu.uapidsl.examples.students.authorization.Authorizer
import edu.byu.uapidsl.examples.students.dto.*
import edu.byu.uapidsl.examples.students.dto.EmailType
import edu.byu.uapidsl.examples.students.dto.EmployeeSummary
import edu.byu.uapidsl.model.validation.Validating
import edu.byu.uapidsl.model.validation.expectMatches
import edu.byu.uapidsl.model.validation.expectNotBlank
import edu.byu.uapidsl.model.validation.expectNotEmpty
import kotlin.reflect.KProperty0

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

        response {
            prop(standardByuId)
          prop<String>("byu_id") {

          }
        }

        idFromModel { it.byuId.value }

//        isRestricted { resource.restricted.value }

        operations {
            read {
                canUserView { authContext.canSeePerson(resource.id) }
                loadModel {
                    println("Loading person for id $id")
                    Database.findPerson(id)?.toDTO(authContext)
                }

                idFromModelField(PersonDTO::byuId)

                idFromModel { it.byuId.value }
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
                canUserCreate { authContext.canCreatePerson() }

                validateInput {
                    expectNotBlank(input::firstName)
                    expectMatches(input::surname, """^\w+$""".toRegex(), "contain only alphanumeric characters")
                }

                handleCreate {
                    createPerson(input, authContext.byuId)
                }
            }

            update<UpdatePerson> {
                canUserUpdate {
                    authContext.canModifyPerson(resource.id)
                }

                canBeUpdated { canPersonBeUpdated(resource.id) }

                validateInput {
                    if (input.firstName != null) {
                        expectNotBlank(input::firstName)
                    }
                    if (expectNotEmpty(input::highSchoolCode)) {
                        expectValidHighSchool(input::highSchoolCode)
                    }
                }

                handleUpdate {
                    updatePerson(resource.id, resource.model, input)
                }

            }

            createOrUpdate<UpdatePerson> {
                validateInput {

                }

                create {
                    canUserCreate {
                        true
                    }

                    handleCreate {

                    }
                }

                update {
                    canUserUpdate {
                        true
                    }

                    canBeUpdated {
                        true
                    }

                    handleUpdate {

                    }
                }
            }

//            createOrUpdate<UpdatePerson> {
//                canUserCreate {
//
//                }
//
//                canUserUpdate {
//
//                }
//
//                canBeUpdated {
//
//                }
//
//                validateInput {
//
//                }
//
//                handleCreate {
//
//                }
//
//                handleUpdate {
//
//                }
//            }

            delete {
                canUserDelete { authContext.canDeletePerson(resource.model.personId.value) }

                canBeDeleted {
                    true
                }

                handleDelete {
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
                canUserCreate {}
                handleCreate {}
            }
            update {
                canUserCreate {}
                handleCreate {}
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

private val highSchools = setOf("PHS", "THS")

fun Validating.expectValidHighSchool(field: KProperty0<String?>): Boolean {
    return expect(field.name, "be a valid High School Code") {
        highSchools.contains(field.get())
    }
}

fun canPersonBeUpdated(byuId: String): Boolean {
    TODO()
}

fun updatePerson(byuId: String, person: PersonDTO, input: UpdatePerson) {
    TODO()
}
