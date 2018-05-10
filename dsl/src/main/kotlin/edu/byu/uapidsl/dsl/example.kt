package edu.byu.uapidsl.dsl

//fun main(args: Array<String>) {
//
//    val model = apiModel<Authz> {
//        authContext {
//            val jwt = it.jwt
//            Authz(
//                    if (jwt.hasResourceOwner()) {
//                        jwt.resourceOwnerClaims!!.personId
//                    } else {
//                        jwt.clientClaims.personId
//                    }
//            )
//        }
//
//        resource<String, PersonDTO>("persons") {
//            operations {
//                create<CreatePerson> {
//                    authorized {
//                        it.authContext.canCreatePerson("")
//                    }
//
//                    handle {
//                        "newId"
//                    }
//                }
//                read {
//                    authorized { true }
//
//                    handle {
////                        loadPerson(it.id)
//                        null
//                    }
//
//                    collection<PersonFilters> { ctx ->
//                        emptyCollection()
//                    }
//
//                    pagedCollection<PersonFilters> {
//                        defaultSize = 100
//                        maxSize = 200
//                        handle { ctx ->
//                            CollectionWithTotal(
//                                    0, emptyCollection()
//                            )
//                        }
//                    }
//                }
//
//                update<UpdatePerson> {
//                    authorized { true }
//
//                    handle {
//
//                    }
//                }
//
//                createOrUpdate<CreateOrUpdatePerson> {
//                    authorized { true }
//                    handle {
//
//                    }
//                }
//
//                delete {
//                    authorized { true }
//                    handle {
//
//                    }
//                }
//            }
//
//            model {
//
//                customizeFields {
//                    it.resource
//                }
//
//                relation<String, RelatedDTO>("my_rel") {
//                    authorized { it: RelationAuthorizationContext<Authz, String, PersonDTO, String, RelatedDTO> ->
//                        true
//                    }
//                    handle { it ->
//                        "relationId" + it.resource.byuId
//                    }
//                }
//                externalRelation("student_info") {
//                    authorized { true }
//                    handle { "https://api.byu.edu/uapi/student/${it.resource.byuId}" }
//                }
//
//                subresource<AddressType, PersonAddressDTO>("addresses") {
//
//                    operations {
//                        createOrUpdate<PutAddress> {
//                            authorized { true }
//                            handle {
//                            }
//                        }
//
//                        read {
//                            authorized { true }
//                            handle {
//                                PersonAddressDTO()
//                            }
//
//                            collection<AddressFilters> {
//                                emptyCollection()
//                            }
//                        }
//
//                        delete {
//                            authorized { true }
//                            handle {
//
//                            }
//                        }
//                    }
//
//                    model {
//                        customizeFields {
//                            it.resource
//                        }
//                    }
//
//                }
//
//            }
//
//        }
//
//    }
//
////model.sparkIt(8080)
////model.graphQlIt(8081)
////model.lambdaIt()
////model.ktor(8080)
//
////model.toOpenAPI3()
////model.toOpenAPI2()
//
//}
//
//class Authz(val userId: String) {
//    fun canReadPerson(byuId: String) = true
//    fun canUpdatePerson(byuId: String) = true
//    fun canCreatePerson(byuId: String) = true
//    fun canDeletePerson(byuId: String) = true
//}
//
//interface CreatePerson
//interface UpdatePerson
//interface CreateOrUpdatePerson
//
//data class PersonFilters(
//        val personId: String?,
//        val netId: String?,
//        val isStudent: Boolean?
//)
//
//enum class AddressType {
//    WRK, RES
//}
//
//class PersonSubDTO
//
//class PersonAddressDTO
//
//class PutAddress
//
//class AddressFilters
//
//class RelatedDTO
//
//class PersonDTO() {
////    val byuId by person.byuId.uapi()
////            .type(ApiType.MODIFIABLE)
////            .description("")
////
////    val netId = UAPIString(person.netId)
////            .type(ApiType.SYSTEM)
//
//    val byuId = ""
//
//
//}
//
//class UAPIString {
//
//}
//
//fun String.uapi(): UAPIDelegate<String> {
//    return UAPIDelegate(this)
//}
//
//class UAPIDelegate<out Type>(val value: Type) {
//    fun type(type: ApiType): UAPIDelegate<Type> {
//        return this
//    }
//}
//
//enum class ApiType {
//    SYSTEM
//}