package edu.byu.uapi.server.resources.identified

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import edu.byu.uapi.server.inputs.DefaultTypeDictionary
import edu.byu.uapi.server.response.ResponseField
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.validation.ValidationEngine
import io.kotlintest.Description
import io.kotlintest.data.forall
import io.kotlintest.specs.DescribeSpec
import io.kotlintest.tables.row
import kotlin.reflect.KClass

class IdentifiedResourceRuntimeSpec : DescribeSpec() {

    private lateinit var resource: IdentifiedResource<User, String, Foo>
    private lateinit var create: IdentifiedResource.Creatable<User, String, Foo, NewFoo>
    private lateinit var update: IdentifiedResource.Updatable<User, String, Foo, Foo>
    private lateinit var delete: IdentifiedResource.Deletable<User, String, Foo>
    private lateinit var list: IdentifiedResource.Listable.Simple<User, String, Foo>
    private lateinit var idReader: IdParamReader<String>

    private lateinit var fixture: IdentifiedResourceRuntime<User, String, Foo>

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
            on { it.getIdReader(any(), any()) } doReturn idReader
            on { it.createOperation } doReturn create
            on { it.updateOperation } doReturn update
            on { it.deleteOperation } doReturn delete
            on { it.listView } doReturn list
            on { it.idType } doReturn String::class
        }

        fixture = IdentifiedResourceRuntime("foo", resource, DefaultTypeDictionary(), ValidationEngine.noop())
    }

    init {
        describe("introspection") {
            context("!availableOperations") {
                it("always includes 'FETCH'") {
                    val foo = FooResource()
                    val runtime = IdentifiedResourceRuntime("foo", foo, DefaultTypeDictionary(), ValidationEngine.noop())
//                    runtime.availableOperations should containExactly(IdentifiedResourceOperation.FETCH)
                }
                it("should find all provided operations") {
                    forall(
                        row(setOf(IdentifiedResourceOperation.CREATE), settable(create = create)),
                        row(setOf(IdentifiedResourceOperation.UPDATE), settable(update = update)),
                        row(setOf(IdentifiedResourceOperation.DELETE), settable(delete = delete)),
                        row(setOf(IdentifiedResourceOperation.LIST), settable(list = list)),

                        row(
                            setOf(
                                IdentifiedResourceOperation.CREATE, IdentifiedResourceOperation.UPDATE, IdentifiedResourceOperation.DELETE
                            ),
                            settable(
                                create = create, update = update, delete = delete
                            )
                        ),
                        row(
                            setOf(
                                IdentifiedResourceOperation.CREATE, IdentifiedResourceOperation.UPDATE, IdentifiedResourceOperation.DELETE, IdentifiedResourceOperation.LIST
                            ),
                            settable(
                                create = create, update = update, delete = delete, list = list
                            )
                        )
                    ) { ops, resource ->
                        val runtime = IdentifiedResourceRuntime("foo", resource, DefaultTypeDictionary(), ValidationEngine.noop())
                        val expected = (ops + IdentifiedResourceOperation.FETCH).toTypedArray()

//                        runtime.availableOperations should containExactlyInAnyOrder(*expected)
                    }
                }
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

    private open class FooResource(val model: Foo? = Foo("bar"), val canUserView: Boolean = true) : IdentifiedResource<User, String, Foo> {
        override val pluralName: String
            get() = "foos"
        override val responseFields: List<ResponseField<User, Foo, *>>
            get() = TODO("not implemented")
        override val idType: KClass<String> = String::class

        override fun loadModel(userContext: User, id: String): Foo? {
            return model
        }

        override fun canUserViewModel(userContext: User, id: String, model: Foo): Boolean {
            return canUserView
        }

        override fun idFromModel(model: Foo): String {
            return model.value
        }

    }

    private class SettableOpsResource(
        val base: IdentifiedResource<User, String, Foo> = FooResource(),
        override val createOperation: IdentifiedResource.Creatable<User, String, Foo, *>? = null,
        override val updateOperation: IdentifiedResource.Updatable<User, String, Foo, *>? = null,
        override val deleteOperation: IdentifiedResource.Deletable<User, String, Foo>? = null,
        override val listView: IdentifiedResource.Listable<User, String, Foo, *>? = null
    ) : IdentifiedResource<User, String, Foo> by base

    private fun settable(
        create: IdentifiedResource.Creatable<User, String, Foo, *>? = null,
        update: IdentifiedResource.Updatable<User, String, Foo, *>? = null,
        delete: IdentifiedResource.Deletable<User, String, Foo>? = null,
        list: IdentifiedResource.Listable<User, String, Foo, *>? = null
    ) = SettableOpsResource(FooResource(), create, update, delete, list)
}
