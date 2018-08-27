import edu.byu.uapi.server.FetchResourceRequest
import edu.byu.uapi.server.IdentifiedResource
import edu.byu.uapi.server.IdentifiedResourceRuntime
import edu.byu.uapi.server.types.UAPINotAuthorizedError
import edu.byu.uapi.server.types.UAPINotFoundError
import edu.byu.uapi.server.types.UAPIPropertiesResponse
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import kotlin.reflect.KClass

class IdentifiedResourceRuntimeSpec: DescribeSpec({
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

}) {
    private class User
    private data class Foo(val value: String)

    private open class FooResource(val model: Foo? = Foo("bar"), val canUserView: Boolean = true): IdentifiedResource<User, String, Foo> {
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
}
