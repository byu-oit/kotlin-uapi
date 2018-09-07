package edu.byu.uapi.server.resources.identified

import edu.byu.uapi.server.response.ResponseFieldDefinition
import edu.byu.uapi.server.validation.Validating
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.kotlintest.tables.row
import kotlin.reflect.KClass

class IdentifiedResourceSpec : DescribeSpec() {
    init {
        describe("operation interface detection") {
            val specializedInterfaces = arrayOf(
                row(IdentifiedResource<String, String, String>::createOperation, IdentifiedResourceSpec::WithCreate),
                row(IdentifiedResource<String, String, String>::updateOperation, IdentifiedResourceSpec::WithUpdate),
                row(IdentifiedResource<String, String, String>::deleteOperation, IdentifiedResourceSpec::WithDelete),
                row(IdentifiedResource<String, String, String>::listView, IdentifiedResourceSpec::WithListable),
                row(IdentifiedResource<String, String, String>::pagedListView, IdentifiedResourceSpec::WithPagedListable)
            )
            it("should be null if the operation interface hasn't been implemented") {
                forall(
                    *specializedInterfaces
                ) { op, _ ->
                    val baseInstance = Base()
                    op.get(baseInstance).shouldBe(null)
                }
            }
            it("should be the runtime instance if the interface has been implemented") {
                forall(
                    *specializedInterfaces
                ) { op, constructor ->
                    val instance = constructor()
                    op.get(instance).shouldBe(instance)
                }
            }
        }
    }

    private open class Base : IdentifiedResource<String, String, String> {
        override val responseFields: List<ResponseFieldDefinition<String, String, *, *>>
            get() = TODO("not implemented")
        override val idType: KClass<String>
            get() = TODO("not implemented")

        override fun loadModel(
            userContext: String,
            id: String
        ): String? {
            TODO("not implemented")
        }

        override fun canUserViewModel(
            userContext: String,
            id: String,
            model: String
        ): Boolean {
            TODO("not implemented")
        }

        override fun idFromModel(model: String): String {
            TODO("not implemented")
        }
    }

    private class WithCreate : Base(),
                               IdentifiedResource.Creatable<String, String, String, String> {
        override fun canUserCreate(userContext: String): Boolean {
            TODO("not implemented")
        }

        override fun validateCreateInput(
            userContext: String,
            input: String,
            validation: Validating
        ) {
            TODO("not implemented")
        }

        override fun handleCreate(
            userContext: String,
            input: String
        ): String {
            TODO("not implemented")
        }

        override val createInput: KClass<String>
            get() = TODO("not implemented")
    }

    private open class WithUpdate : Base(),
                                    IdentifiedResource.Updatable<String, String, String, String> {
        override fun canUserUpdate(
            userContext: String,
            id: String,
            model: String
        ): Boolean {
            TODO("not implemented")
        }

        override fun canBeUpdated(
            id: String,
            model: String
        ): Boolean {
            TODO("not implemented")
        }

        override fun validateUpdateInput(
            userContext: String,
            id: String,
            model: String,
            input: String,
            validation: Validating
        ) {
            TODO("not implemented")
        }

        override fun handleUpdate(
            userContext: String,
            id: String,
            model: String,
            input: String
        ) {
            TODO("not implemented")
        }

        override val updateInput: KClass<String>
            get() = TODO("not implemented")

    }

    private class WithCreateWithId : WithUpdate(),
                                     IdentifiedResource.UpdatableOrCreatable<String, String, String, String> {
        override fun canUserCreateWithId(
            userContext: String,
            id: String
        ): Boolean {
            TODO("not implemented")
        }

        override fun validateCreateWithIdInput(
            userContext: String,
            id: String,
            input: String,
            validation: Validating
        ) {
            TODO("not implemented")
        }

        override fun handleCreateWithId(
            userContext: String,
            input: String,
            id: String
        ) {
            TODO("not implemented")
        }


    }

    private class WithDelete : Base(),
                               IdentifiedResource.Deletable<String, String, String> {
        override fun canUserDelete(
            userContext: String,
            id: String,
            model: String
        ): Boolean {
            TODO("not implemented")
        }

        override fun canBeDeleted(
            id: String,
            model: String
        ): Boolean {
            TODO("not implemented")
        }

        override fun handleDelete(
            userContext: String,
            id: String,
            model: String
        ) {
            TODO("not implemented")
        }

    }

    private class WithListable : Base(),
                                 IdentifiedResource.Listable<String, String, String, String> {
        override fun list(
            userContext: String,
            filters: String
        ): Collection<String> {
            TODO("not implemented")
        }

        override val filterType: KClass<String>
            get() = TODO("not implemented")

    }

    private class WithPagedListable : Base(),
                                      IdentifiedResource.PagedListable<String, String, String, String> {
        override fun list(
            userContext: String,
            filters: String,
            paging: PagingParams
        ): CollectionWithTotal<String> {
            TODO("not implemented")
        }

        override val filterType: KClass<String>
            get() = TODO("not implemented")
        override val defaultPageSize: Int
            get() = TODO("not implemented")
        override val maxPageSize: Int
            get() = TODO("not implemented")

    }
}


