package edu.byu.uapi.server.resources.list

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import edu.byu.uapi.server.inputs.DefaultTypeDictionary
import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.validation.ValidationEngine
import io.kotlintest.Description
import io.kotlintest.specs.DescribeSpec
import kotlin.reflect.KClass

class ListResourceRuntimeSpec : DescribeSpec() {

    private lateinit var resource: ListResource<User, String, Foo, *>
    private lateinit var create: ListResource.Creatable<User, String, Foo, NewFoo>
    private lateinit var update: ListResource.Updatable<User, String, Foo, Foo>
    private lateinit var delete: ListResource.Deletable<User, String, Foo>
    private lateinit var list: ListResource.Simple<User, String, Foo>
    private lateinit var idReader: IdParamReader<String>

    private lateinit var fixture: ListResourceRuntime<User, String, Foo, *>

    override fun beforeTest(description: Description) {
        super.beforeTest(description)

        create = mock {
            on { createInput } doReturn NewFoo::class
        }
        update = mock()
        delete = mock()
        list = mock()

        idReader = mock()

        resource = mock {
            on { it.getIdReader(any()) } doReturn idReader
            on { it.createOperation } doReturn create
            on { it.updateOperation } doReturn update
            on { it.deleteOperation } doReturn delete
            on { it.idType } doReturn String::class
        }

        fixture = ListResourceRuntime(resource, DefaultTypeDictionary(), ValidationEngine.noop())
    }

    init {
        describe("introspection") {
            context("!availableOperations") {
                it("always includes 'FETCH'") {
                    val foo = FooResource()
                    val runtime = ListResourceRuntime(foo, DefaultTypeDictionary(), ValidationEngine.noop())
//                    runtime.availableOperations should containExactly(IdentifiedResourceOperation.FETCH)
                }
                it("should find all provided operations") /* {
                    forall(
                        row(setOf(ListResourceOperation.CREATE), settable(create = create)),
                        row(setOf(ListResourceOperation.UPDATE), settable(update = update)),
                        row(setOf(ListResourceOperation.DELETE), settable(delete = delete)),

                        row(
                            setOf(
                                ListResourceOperation.CREATE, ListResourceOperation.UPDATE, ListResourceOperation.DELETE
                            ),
                            settable(
                                create = create, update = update, delete = delete
                            )
                        ),
                        row(
                            setOf(
                                ListResourceOperation.CREATE, ListResourceOperation.UPDATE, ListResourceOperation.DELETE, ListResourceOperation.LIST
                            ),
                            settable(
                                create = create, update = update, delete = delete
                            )
                        )
                    ) { ops, resource ->
                        val runtime = ListResourceRuntime(resource, DefaultTypeDictionary(), ValidationEngine.noop())
                        val expected = (ops + ListResourceOperation.FETCH).toTypedArray()

//                        runtime.availableOperations should containExactlyInAnyOrder(*expected)
                    }
                } */
            }
        }

//        describe("handleFetchRequest") {
//            it("returns a response when record is found") {
//                val record = Foo("hi")
//
//                whenever(resource.loadModel(any(), any())).thenReturn(record)
//                whenever(resource.canUserViewModel(any(), any(), any())).thenReturn(true)
//
//                val resp = fixture.handleFetchRequest(FetchResourceRequest(
//                    User(), "1"
//                ))
//
//                resp.metadata.validationResponse.code shouldBe 200
//
//                resp should beInstanceOf(UAPIPropertiesResponse::class)
//                (resp as UAPIPropertiesResponse<*>).properties shouldBe Foo("hi")
//            }
//            it("should return an error response if a record is not found") {
//                whenever(resource.loadModel(any(), any())).thenReturn(null)
//                whenever(resource.canUserViewModel(any(), any(), any())).thenReturn(true)
//
//                val resp = fixture.handleFetchRequest(FetchResourceRequest(User(), "1"))
//
//                resp shouldBe UAPINotFoundError
//            }
//            it("should return an error response if the user is not authorized") {
//                whenever(resource.loadModel(any(), any())).thenReturn(Foo("1"))
//
//                whenever(resource.canUserViewModel(any(), any(), any())).thenReturn(false)
//
//                val resp = fixture.handleFetchRequest(FetchResourceRequest(User(), "1"))
//
//                resp shouldBe UAPINotAuthorizedError
//            }
//        }
//
//        describe("handleCreateRequest") {
//            it("handles a simple create") {
//                val new = NewFoo("value")
//                val foo = Foo("value")
//
//                whenever(resource.loadModel(any(), any())).thenReturn(foo)
//                whenever(create.canUserCreateWithId(any())).thenReturn(true)
//                whenever(create.handleCreateWithId(any(), any())).thenReturn("foo")
//
//                fixture.handleCreateRequest(CreateResourceRequest(
//                    User(), new
//                ))
//            }
//        }
    }

    private class User
    private data class Foo(val value: String)
    private data class NewFoo(val value: String)

    private open class FooResource(val model: Foo? = Foo("bar"), val canUserView: Boolean = true) : ListResource.Simple<User, String, Foo> {

        override val pluralName: String
            get() = "foos"
        override val responseFields: List<ResponseField<User, Foo, *>>
            get() = TODO("not implemented")
        override val idType: KClass<String> = String::class

        override fun loadModel(
            userContext: User,
            id: String
        ): Foo? {
            return model
        }

        override fun canUserViewModel(
            userContext: User,
            id: String,
            model: Foo
        ): Boolean {
            return canUserView
        }

        override fun idFromModel(model: Foo): String {
            return model.value
        }

         override fun list(
            userContext: User,
            params: ListParams.Empty
        ): List<Foo> {
            TODO("not implemented")
        }

    }

    private class SettableOpsResource(
        val base: ListResource<User, String, Foo, ListParams.Empty> = FooResource(),
        override val createOperation: ListResource.Creatable<User, String, Foo, *>? = null,
        override val updateOperation: ListResource.Updatable<User, String, Foo, *>? = null,
        override val deleteOperation: ListResource.Deletable<User, String, Foo>? = null
    ) : ListResource<User, String, Foo, ListParams.Empty> by base

    private fun settable(
        create: ListResource.Creatable<User, String, Foo, *>? = null,
        update: ListResource.Updatable<User, String, Foo, *>? = null,
        delete: ListResource.Deletable<User, String, Foo>? = null
    ) = SettableOpsResource(FooResource(), create, update, delete)
}
