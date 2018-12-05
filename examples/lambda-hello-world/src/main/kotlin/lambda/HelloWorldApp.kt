package lambda

import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.UserContextAuthnInfo
import edu.byu.uapi.server.UserContextFactory
import edu.byu.uapi.server.UserContextResult
import edu.byu.uapi.server.resources.list.ListResource
import edu.byu.uapi.server.resources.list.fields
import edu.byu.uapi.server.subresources.list.fields
import edu.byu.uapi.server.subresources.singleton.fields
import edu.byu.uapi.server.types.CreateResult
import edu.byu.uapi.server.types.DeleteResult
import edu.byu.uapi.server.types.IdentifiedModel
import edu.byu.uapi.server.types.UpdateResult
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.*
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.scalars.ScalarFormat
import java.util.*
import kotlin.reflect.KClass

enum class GreetingContexts {
    FOO, BAZ
}

val helloWorldRuntime = UAPIRuntime(HelloWorldUserFactory()).also {
    it.resource(GreetingResource()) {
        subresource(FooSubresource())
        subresource(BarSubresource())
    }
//    it.resourceWithContexts(GreetingResource(), GreetingContexts::class) {
//        subresource(FooSubresource(), GreetingContexts.FOO, GreetingContexts.BAZ)
//        subresource(BarSubresource(), GreetingContexts.FOO)
//    }
}

class HelloWorldUserFactory : UserContextFactory<HelloWorldUser> {
    override fun createUserContext(authenticationInfo: UserContextAuthnInfo): UserContextResult<HelloWorldUser> {
        return UserContextResult.Success(HelloWorldUser("Cosmo"))
    }
}

data class Greeting(
    val lang: Language,
    val greeting: String = getGreeting(lang)
)

data class HelloWorldUser(
    val firstName: String
)

enum class Language {
    EN,
    IT
}

fun getGreeting(lang: Language) = when (lang) {
    Language.EN -> "Hello"
    Language.IT -> "Ciao"
}

data class Foo(
    val bar: String,
    val baz: UUID = UUID.randomUUID()
)

private val foos = mutableMapOf(
    Language.IT to Foo("sbarro")
)

class FooSubresource : ListResource.SingletonSubresource<HelloWorldUser, String, Greeting, Foo>,
                       ListResource.SingletonSubresource.Updatable<HelloWorldUser, String, Greeting, Foo, Foo>,
                       ListResource.SingletonSubresource.Creatable<HelloWorldUser, String, Greeting, Foo, Foo>,
                       ListResource.SingletonSubresource.Deletable<HelloWorldUser, String, Greeting, Foo> {
    override val name: String = "foo"

    override fun loadModel(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>
    ): Foo? {
        return foos[parent.model.lang]
    }

    override fun canUserViewModel(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>,
        model: Foo
    ): Boolean {
        return true
    }

    override val responseFields = fields {
        value(Foo::bar) {}
        value(Foo::baz) {}
    }

    override fun canUserUpdate(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>,
        model: Foo
    ): Boolean {
        return true
    }

    override fun canBeUpdated(
        parent: IdentifiedModel<String, Greeting>,
        model: Foo
    ): Boolean {
        return true
    }

    override fun handleUpdate(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>,
        model: Foo,
        input: Foo
    ): UpdateResult<Foo> {
        foos[parent.model.lang] = input
        return UpdateResult.Success(input)
    }

    override fun canUserCreate(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>
    ): Boolean {
        return true
    }

    override fun handleCreate(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>,
        input: Foo
    ): CreateResult<Foo> {
        foos[parent.model.lang] = input
        return CreateResult.Success(input)
    }

    override fun canUserDelete(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>,
        model: Foo
    ): Boolean {
        return true
    }

    override fun canBeDeleted(
        parent: IdentifiedModel<String, Greeting>,
        model: Foo
    ): Boolean {
        return true
    }

    override fun handleDelete(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>,
        model: Foo
    ): DeleteResult {
        foos.remove(parent.model.lang) ?: return DeleteResult.AlreadyDeleted
        return DeleteResult.Success
    }
}

class GreetingResource : ListResource.Simple<HelloWorldUser, String, Greeting> {

    override val pluralName = "greetings"

    override fun list(
        userContext: HelloWorldUser,
        params: ListParams.Empty
    ): List<Greeting> = Language.values().map { Greeting(it) }

    override fun loadModel(
        userContext: HelloWorldUser,
        id: String
    ): Greeting? {
        val lang = try {
            Language.valueOf(id)
        } catch (err: Exception) {
            err.printStackTrace()
            return null
        }

        return Greeting(lang)
    }

    override fun canUserViewModel(
        userContext: HelloWorldUser,
        id: String,
        model: Greeting
    ) = true

    override fun idFromModel(model: Greeting): String {
        return model.lang.name
    }

    override val responseFields = fields {
        value<Language>("lang") {
            key = true
            getValue { it.lang }
        }
        value<String>("greeting") {
            getValue { it.greeting }
        }
    }

    override val idType: KClass<String> = String::class

    override fun getIdReader(
        dictionary: TypeDictionary
    ): IdParamReader<String> {
        return object : IdParamReader<String> {
            override fun read(input: IdParams): String {
                return input.getValue("id").asString()
            }

            override fun describe(): IdParamMeta {
                return IdParamMeta.Default(listOf(IdParamMeta.Param("id", ScalarFormat.STRING)))
            }
        }
    }

    override val createOperation: ListResource.Creatable<HelloWorldUser, String, Greeting, *>? = null
    override val updateOperation: ListResource.Updatable<HelloWorldUser, String, Greeting, *>? = null
    override val deleteOperation: ListResource.Deletable<HelloWorldUser, String, Greeting>? = null

    override fun getListParamReader(dictionary: TypeDictionary): ListParamReader<ListParams.Empty> {
        return EmptyListParamReader
    }

    override val listParamsType: KClass<ListParams.Empty> = ListParams.Empty::class
}

data class Bar(
    val id: BarId,
    val baz: String
)

data class BarId(
    val str: String,
    val num: Int
)

private val bars = listOf(
    Bar(BarId("a", 1), "Mars"),
    Bar(BarId("a", 2), "Milky Way"),
    Bar(BarId("b", 1), "Baby Ruth")
).associateBy { it.id }.toMutableMap()

class BarSubresource: ListResource.ListSubresource.Simple<HelloWorldUser, String, Greeting, BarId, Bar> {
    override val pluralName: String = "bars"

    override fun loadModel(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>,
        id: BarId
    ): Bar? = bars[id]

    override fun canUserViewModel(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>,
        id: BarId,
        model: Bar
    ): Boolean {
        return true
    }

    override fun idFromModel(model: Bar): BarId {
        return model.id
    }

    override fun list(
        userContext: HelloWorldUser,
        parent: IdentifiedModel<String, Greeting>,
        params: ListParams.Empty
    ): List<Bar> {
        return bars.values.toList()
    }

    override val responseFields = fields {
        value(Bar::id, BarId::str) {
            key = true
        }
        value(Bar::id, BarId::num) {
            key = true
        }

        value(Bar::baz) {}
    }

}
