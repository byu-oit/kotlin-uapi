package edu.byu.uapidsl.examples.students

import edu.byu.uapidsl.apiModel
import edu.byu.uapidsl.examples.students.app.*
import edu.byu.uapidsl.examples.students.authorization.Authorizer
import edu.byu.uapidsl.examples.students.dto.*

val personsModel = apiModel<Authorizer> {

  authContext {
    Authorizer(it.jwt)
  }

  resource<String, PersonDTO>("persons") {
    operations {

      read {

        handle {
          loadPerson(it.id, it.authContext.canSeeRestrictedRecords())
        }
      }

      listPaged<PersonListFilters> {
        defaultSize = 50
        maxSize = 200
        handle {
          queryPeople(it.filters, it.paging, it.authContext.canSeeRestrictedRecords())
        }
      }


      create<CreatePerson> {
        authorization { it.authContext.canCreatePerson() }
        handle { ctx ->
          createPerson(ctx.input, ctx.authContext.byuId)
        }
      }

      update<UpdatePerson> {
        authorization { it.authContext.canModifyPerson(it.id) }
        handle {
          TODO()
        }
      }

      delete {
        authorization { it.authContext.canDeletePerson(it.resource.personId) }
      }

    }

    model {
      collectionSubresource<AddressType, PersonAddressDTO>("addresses") {

        operations {
          listSimple<Unit> {
            listPersonAddressTypes(it.parentId)
          }
          read {
            handle {
              getPersonAddress(it.parentId, it.id)
            }
          }
        }
      }

      collectionSubresource<PersonCredentialId, PersonCredentialDTO>("credentials") {
        authorization { it.authContext.canSeeCredentialsFor(it.parentId) }

        operations {
          listPaged<Unit> {
            maxSize = 20
            defaultSize = 20

            handle {
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
            TODO("Not Implemented")
          }
        }
      }

      singleSubresource<EmployeeSummary>("employee_summaries") {
        operations {
          read {
            authorization { it.authContext.canSeeEmployeeInfo(it.parentId) }

            handle {
              TODO()
            }
          }
        }
      }
    }
  }
}

