package edu.byu.kotlin.uapidsl

fun main(args: Array<String>) {

    val model = apiModel<Authz> {
        authContext {
            val jwt = it.jwt
            Authz(
                    if (jwt.hasResourceOwner()) {
                        jwt.resourceOwnerClaims.personId
                    } else {
                        jwt.clientClaims.personId
                    }
            )
        }

        resource<String, PersonDTO>("persons") {
            operations {
                create<CreatePerson> {
                    authorization {
                        it.authContext.canCreatePerson("")
                    }

                    handle {
                        "newId"
                    }
                }
                read {
                    authorization { true }

                    handle {
                        loadPerson(it.id)
                    }

                    collection<PersonFilters> { ctx ->
                        emptySequence()
                    }

                    pagedCollection<PersonFilters> {
                        defaultSize = 100
                        maxSize = 200
                        handle { ctx ->
                            SequenceWithTotal(
                                    0, emptySequence()
                            )
                        }
                    }
                }

                update<UpdatePerson> {
                    authorization { true }

                    handle {

                    }
                }

                createOrUpdate<CreateOrUpdatePerson> {
                    authorization { true }
                    handle {

                    }
                }

                delete {
                    authorization { true }
                    handle {

                    }
                }
            }

            model {

                customizeFields {
                    it.resource
                }

                relation<String, RelatedDTO>("my_rel") {
                    authorization { it: RelationAuthorizationContext<Authz, String, PersonDTO, String, RelatedDTO> ->
                        true
                    }
                    handle { it ->
                        "relationId" + it.resource.byuId
                    }
                }
                externalRelation("student_info") {
                    authorization { true }
                    handle { "https://api.byu.edu/uapi/student/${it.resource.byu_id}" }
                }

                subresource<AddressType, PersonAddressDTO>("addresses") {

                    operations {
                        createOrUpdate<PutAddress> {
                            authorization { true }
                            handle {
                            }
                        }

                        read {
                            authorization { true }
                            handle {
                                PersonAddressDTO()
                            }

                            collection<AddressFilters> {
                                emptySequence()
                            }
                        }

                        delete {
                            authorization { true }
                            handle {

                            }
                        }
                    }

                    model {
                        customizeFields {
                            it.resource
                        }
                    }

                }

            }

        }

    }

//model.sparkIt(8080)
//model.graphQlIt(8081)
//model.lambdaIt()
//model.ktor(8080)

//model.toOpenAPI3()
//model.toOpenAPI2()

}

class Authz(val userId: String) {
    fun canReadPerson(byuId: String) = true
    fun canUpdatePerson(byuId: String) = true
    fun canCreatePerson(byuId: String) = true
    fun canDeletePerson(byuId: String) = true
}

interface CreatePerson
interface UpdatePerson
interface CreateOrUpdatePerson

data class PersonFilters(
        val personId: String?,
        val netId: String?,
        val isStudent: Boolean?
)

enum class AddressType {
    WRK, RES
}

class PersonSubDTO

class PersonAddressDTO

class PutAddress

class AddressFilters

class RelatedDTO

class PersonDTO(person: Person) {
    val byuId by person.byuId.uapi()
            .type(ApiType.MODIFIABLE)
            .description("")

    val netId = UAPIString(person.netId)
            .type(ApiType.SYSTEM)


}

class UAPIString {

}

fun String.uapi(): UAPIDelegate<String> {
    return UAPIDelegate(this)
}

class UAPIDelegate<out Type>(val value: Type) {
    fun type(type: ApiType): UAPIDelegate<Type> {
        return this
    }
}

enum class ApiType {
    SYSTEM
}
