package edu.byu.uapi.server

import edu.byu.uapi.server.inputs.DefaultTypeDictionary
import edu.byu.uapi.server.resources.identified.IdentifiedResource
import edu.byu.uapi.server.resources.identified.IdentifiedResourceRuntime
import edu.byu.uapi.spi.requests.Headers
import edu.byu.uapi.spi.dictionary.TypeDictionary
import java.util.*

class UAPIRuntime<UserContext : Any>(
    val userContextFactory: UserContextFactory<UserContext>,
    val typeDictionary: TypeDictionary = DefaultTypeDictionary()
) {
    constructor(fn: UserContextFactoryFunc<UserContext>) : this(UserContextFactory.from(fn))

    private val resources: MutableMap<String, IdentifiedResourceRuntime<UserContext, *, *>> = mutableMapOf()

    fun register(
        name: String,
        resource: IdentifiedResource<UserContext, *, *>
    ) {
        val runtime = IdentifiedResourceRuntime(name, resource, typeDictionary)
        resources[name] = runtime
        //TODO: Validate resource
    }

    fun resources(): Map<String, IdentifiedResourceRuntime<UserContext, *, *>> = Collections.unmodifiableMap(resources)
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

