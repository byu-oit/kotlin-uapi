package edu.byu.uapi.server

import edu.byu.uapi.server.inputs.DefaultTypeDictionary
import edu.byu.uapi.server.resources.list.ListResource
import edu.byu.uapi.server.resources.list.ListResourceRuntime
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.requests.Headers
import edu.byu.uapi.spi.scalars.ScalarType
import edu.byu.uapi.spi.validation.ValidationEngine
import edu.byu.uapi.validation.hibernate.HibernateValidationEngine
import java.util.*

class UAPIRuntime<UserContext : Any>(
    options: Options<UserContext>
) {

    constructor(userContextFactory: UserContextFactory<UserContext>) : this(Options(userContextFactory))
//    constructor(init: RuntimeInit<UserContext>.() -> Unit) : this(RuntimeInit<UserContext>().apply { init() }.build())

    companion object {
        inline operator fun <UserContext : Any> invoke(init: RuntimeInit<UserContext>.() -> Unit): UAPIRuntime<UserContext> {
            val ri = RuntimeInit<UserContext>()
            ri.init()
            return ri.build()
        }
    }

    val userContextFactory = options.userContextFactory
    val typeDictionary = options.typeDictionary
    val validationEngine = options.validationEngine

    private val resources: MutableMap<String, ListResourceRuntime<UserContext, *, *, *>> = mutableMapOf()

    fun register(
        resource: ListResource<UserContext, *, *, *>
    ) {
        val runtime = ListResourceRuntime(resource.pluralName, resource, typeDictionary, validationEngine)
        resources[resource.pluralName] = runtime
        //TODO: Validate resource
    }

    fun resources(): Map<String, ListResourceRuntime<UserContext, *, *, *>> = Collections.unmodifiableMap(resources)

    data class Options<UserContext : Any>(
        val userContextFactory: UserContextFactory<UserContext>,
        val typeDictionary: TypeDictionary = DefaultTypeDictionary(),
        val validationEngine: ValidationEngine = HibernateValidationEngine
    ) {
        constructor(
            userContextFactory: UserContextFactoryFunc<UserContext>,
            typeDictionary: TypeDictionary = DefaultTypeDictionary(),
            validationEngine: ValidationEngine = HibernateValidationEngine
        ) : this(
            userContextFactory = UserContextFactory.from(userContextFactory),
            typeDictionary = typeDictionary,
            validationEngine = validationEngine
        )
    }
}

class RuntimeInit<UserContext : Any> {

    lateinit var userContextFactory: UserContextFactory<UserContext>

//    fun userContextFactory(factoryFunc: UserContextFactoryFunc<UserContext>) {
//        userContextFactory = UserContextFactory.from(factoryFunc)
//    }

    var typeDictionary: TypeDictionary = DefaultTypeDictionary()
    var validationEngine: ValidationEngine = HibernateValidationEngine

    private val scalars: MutableList<ScalarType<*>> = mutableListOf()

    operator fun ScalarType<*>.unaryPlus() {
        scalars.add(this)
    }

    private val resources: MutableList<ListResource<UserContext, *, *, *>> = mutableListOf()

    operator fun ListResource<UserContext, *, *, *>.unaryPlus() {
        resources += this
    }

    fun build(): UAPIRuntime<UserContext> {
        scalars.forEach { typeDictionary.registerScalarType(it) }
        return UAPIRuntime(UAPIRuntime.Options(
            userContextFactory = userContextFactory,
            typeDictionary = typeDictionary,
            validationEngine = validationEngine
        )).also { r ->
            resources.forEach { r.register(it) }
        }
    }
}

typealias UserContextFactoryFunc<UserContext> = (UserContextAuthnInfo) -> UserContextResult<UserContext>

interface UserContextFactory<out UserContext : Any> {
    fun createUserContext(authenticationInfo: UserContextAuthnInfo): UserContextResult<UserContext>

    companion object {
        fun <UserContext : Any> from(fn: UserContextFactoryFunc<UserContext>): UserContextFactory<UserContext> {
            return FunctionUserContextFactory(fn)
        }
    }

    private class FunctionUserContextFactory<out UserContext : Any>(
        private val fn: UserContextFactoryFunc<UserContext>
    ) : UserContextFactory<UserContext> {
        override fun createUserContext(authenticationInfo: UserContextAuthnInfo): UserContextResult<UserContext> {
            return fn(authenticationInfo)
        }
    }

}

sealed class UserContextResult<out UserContext : Any> {
    data class Success<out UserContext : Any>(
        val result: UserContext
    ) : UserContextResult<UserContext>()

    data class Failure(
        val messages: List<String>
    ) : UserContextResult<Nothing>() {
        constructor(message: String) : this(listOf(message))
        constructor(vararg messages: String) : this(messages.toList())
    }
}

interface UserContextAuthnInfo {
    val headers: Headers
    val queryParams: Map<String, Set<String>>
    val requestUrl: String
    val relativePath: String
    val remoteIp: String
}

