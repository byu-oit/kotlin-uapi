package edu.byu.uapi.server.resources.identified

import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.spi.input.ListParams
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
                row(IdentifiedResource<String, String, String>::listView, IdentifiedResourceSpec::WithListable)
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
        override val responseFields: List<ResponseField<String, String, *>>
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

        override fun handleCreate(
            userContext: String,
            input: String
        ): CreateResult<String> {
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

        override fun handleUpdate(
            userContext: String,
            id: String,
            model: String,
            input: String
        ): UpdateResult {
            TODO("not implemented")
        }

        override val updateInput: KClass<String>
            get() = TODO("not implemented")

    }

    private class WithCreateWithId : WithUpdate(),
                                     IdentifiedResource.CreatableWithId<String, String, String, String> {
        override fun canUserCreateWithId(
            userContext: String,
            id: String
        ): Boolean {
            TODO("not implemented")
        }

        override fun handleCreateWithId(
            userContext: String,
            input: String,
            id: String
        ): CreateResult<String> {
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
        ): DeleteResult {
            TODO("not implemented")
        }
    }

    private class WithListable : Base(),
                                 IdentifiedResource.Listable.Simple<String, String, String> {
        override fun list(
            userContext: String,
            params: ListParams.Empty
        ): List<String> {
            TODO("not implemented")
        }

    }
}


