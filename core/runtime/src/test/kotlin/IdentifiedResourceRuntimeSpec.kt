import com.nhaarman.mockitokotlin2.mock
import edu.byu.uapi.server.FetchResourceRequest
import edu.byu.uapi.server.IdentifiedResource
import edu.byu.uapi.server.IdentifiedResourceRuntime
import edu.byu.uapi.server.IdentifiedResourceRuntime.Operation
import edu.byu.uapi.server.types.UAPINotAuthorizedError
import edu.byu.uapi.server.types.UAPINotFoundError
import edu.byu.uapi.server.types.UAPIPropertiesResponse
import io.kotlintest.Description
import io.kotlintest.data.forall
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.collections.containExactly
import io.kotlintest.matchers.collections.containExactlyInAnyOrder
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.kotlintest.tables.row
import kotlin.reflect.KClass

class IdentifiedResourceRuntimeSpec : DescribeSpec() {

    private lateinit var create: IdentifiedResource.Creatable<User, String, Foo, Foo>
    private lateinit var update: IdentifiedResource.Updatable<User, String, Foo, Foo>
    private lateinit var delete: IdentifiedResource.Deletable<User, String, Foo>
    private lateinit var list: IdentifiedResource.Listable<User, String, Foo, Foo>
    private lateinit var pagedList: IdentifiedResource.PagedListable<User, String, Foo, Foo>

    override fun beforeTest(description: Description) {
        super.beforeTest(description)

        create = mock()
        update = mock()
        delete = mock()
        list = mock()
        pagedList = mock()
    }

    init {
        describe("handleFetchRequest") {
            it("returns a response when record is found") {
                val res = FooResource(
                    model = Foo("hi")
                )

                val runtime = IdentifiedResourceRuntime(res)
                val resp = runtime.handleFetchRequest(FetchResourceRequest(
                    User(), "1"
                ))

                resp.metadata.validationResponse.code shouldBe 200

                resp should beInstanceOf(UAPIPropertiesResponse::class)
                (resp as UAPIPropertiesResponse<*>).properties shouldBe Foo("hi")
            }
            it("should return an error response if a record is not found") {
                val res = FooResource(
                    model = null
                )

                val runtime = IdentifiedResourceRuntime(res)

                val resp = runtime.handleFetchRequest(FetchResourceRequest(User(), "1"))

                resp shouldBe UAPINotFoundError
            }
            it("should return an error response if the user is not authorized") {
                val res = FooResource(
                    canUserView = false
                )

                val runtime = IdentifiedResourceRuntime(res)

                val resp = runtime.handleFetchRequest(FetchResourceRequest(User(), "1"))

                resp shouldBe UAPINotAuthorizedError
            }
        }

        describe("availableOperations") {
            it("always includes 'FETCH'") {
                val foo = FooResource()
                val runtime = IdentifiedResourceRuntime(foo)
                runtime.availableOperations should containExactly(Operation.FETCH)
            }
            it("should find all provided operations") {
                forall(
                    row(setOf(Operation.CREATE), settable(create = create)),
                    row(setOf(Operation.UPDATE), settable(update = update)),
                    row(setOf(Operation.DELETE), settable(delete = delete)),
                    row(setOf(Operation.LIST), settable(list = list)),
                    row(setOf(Operation.LIST), settable(pagedList = pagedList)),

                    row(
                        setOf(
                            Operation.CREATE, Operation.UPDATE, Operation.DELETE
                        ),
                        settable(
                            create = create, update = update, delete = delete
                        )
                    ),
                    row(
                        setOf(
                            Operation.CREATE, Operation.UPDATE, Operation.DELETE, Operation.LIST
                        ),
                        settable(
                            create = create, update = update, delete = delete, list = list
                        )
                    )
                ) { ops, resource ->
                    val runtime = IdentifiedResourceRuntime(resource)
                    val expected = (ops + Operation.FETCH).toTypedArray()

                    runtime.availableOperations should containExactlyInAnyOrder(*expected)
                }
            }
        }

    }


    private class User
    private data class Foo(val value: String)

    private open class FooResource(val model: Foo? = Foo("bar"), val canUserView: Boolean = true) : IdentifiedResource<User, String, Foo> {
        override val idType: KClass<String> = String::class
        override val modelType: KClass<Foo> = Foo::class

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
        override val createOperation: IdentifiedResource.Creatable<User, String, Foo, *>? = null,
        override val createWithIdOperation: IdentifiedResource.CreatableWithId<User, String, Foo, *>? = null,
        override val updateOperation: IdentifiedResource.Updatable<User, String, Foo, *>? = null,
        override val deleteOperation: IdentifiedResource.Deletable<User, String, Foo>? = null,
        override val listView: IdentifiedResource.Listable<User, String, Foo, *>? = null,
        override val pagedListView: IdentifiedResource.PagedListable<User, String, Foo, *>? = null
    ) : FooResource()

    private fun settable(
        create: IdentifiedResource.Creatable<User, String, Foo, *>? = null,
        createWithId: IdentifiedResource.CreatableWithId<User, String, Foo, *>? = null,
        update: IdentifiedResource.Updatable<User, String, Foo, *>? = null,
        delete: IdentifiedResource.Deletable<User, String, Foo>? = null,
        list: IdentifiedResource.Listable<User, String, Foo, *>? = null,
        pagedList: IdentifiedResource.PagedListable<User, String, Foo, *>? = null
    ) = SettableOpsResource(create, createWithId, update, delete, list, pagedList)
}
