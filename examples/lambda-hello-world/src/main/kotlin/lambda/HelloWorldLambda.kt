package lambda

import edu.byu.uapi.http.awslambdaproxy.LambdaConfig
import edu.byu.uapi.http.awslambdaproxy.LambdaProxyEngine
import edu.byu.uapi.http.awslambdaproxy.UAPILambdaHandler
import edu.byu.uapi.http.json.JavaxJsonTreeEngine
import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.UserContextAuthnInfo
import edu.byu.uapi.server.UserContextFactory
import edu.byu.uapi.server.UserContextResult
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.fields
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.*
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.scalars.ScalarFormat
import org.pmw.tinylog.Configurator
import org.pmw.tinylog.Level
import org.pmw.tinylog.Logger
import kotlin.reflect.KClass

class HelloWorldLambda : UAPILambdaHandler(
    LambdaConfig(JavaxJsonTreeEngine)
) {
    init {
        Configurator.defaultConfig().level(Level.DEBUG).activate()
    }
    override fun setup(engine: LambdaProxyEngine) {
        Logger.info("Starting Setup")
        val runtime = UAPIRuntime(
            HelloWorldUserFactory()
        )
        runtime.register("greetings", GreetingResource())

        engine.register(runtime)
        Logger.info("Finished Setup")
    }
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

class GreetingResource : IdentifiedResource<HelloWorldUser, String, Greeting>,
                         IdentifiedResource.Listable.Simple<HelloWorldUser, String, Greeting> {
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
        dictionary: TypeDictionary,
        paramPrefix: String
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

    override val createOperation: IdentifiedResource.Creatable<HelloWorldUser, String, Greeting, *>? = null
    override val updateOperation: IdentifiedResource.Updatable<HelloWorldUser, String, Greeting, *>? = null
    override val deleteOperation: IdentifiedResource.Deletable<HelloWorldUser, String, Greeting>? = null
    override val listView: IdentifiedResource.Listable<HelloWorldUser, String, Greeting, *>? = this

    override fun getListParamReader(dictionary: TypeDictionary): ListParamReader<ListParams.Empty> {
        return EmptyListParamReader
    }

    override val listParamsType: KClass<ListParams.Empty> = ListParams.Empty::class
}
