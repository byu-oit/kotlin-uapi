package edu.byu.uapi.server

import edu.byu.jwt.ByuJwt

class UAPIRuntime<UserContext : Any>(
    val userContextFactory: UserContextFactory<UserContext>
) {

    constructor(fn: (UserContextAuthnInfo) -> UserContext) : this(UserContextFactory.from(fn))

    private val resources: MutableMap<String, IdentifiedResourceRuntime<UserContext, *, *>> = mutableMapOf()

    fun register(name: String, resource: IdentifiedResource<UserContext, *, *>) {
        val runtime = IdentifiedResourceRuntime(name, resource)
        resources[name] = runtime
        //TODO: Validate resource
    }
}

interface UserContextFactory<out UserContext : Any> {
    fun createUserContext(authenticationInfo: UserContextAuthnInfo): UserContext

    companion object {
        fun <UserContext : Any> from(fn: (UserContextAuthnInfo) -> UserContext): UserContextFactory<UserContext> {
            return FunctionUserContextFactory(fn)
        }
    }

    private class FunctionUserContextFactory<out UserContext : Any>(
        private val fn: (UserContextAuthnInfo) -> UserContext
    ) : UserContextFactory<UserContext> {
        override fun createUserContext(authenticationInfo: UserContextAuthnInfo): UserContext {
            return fn(authenticationInfo)
        }
    }
}


interface UserContextAuthnInfo {
    val jwt: ByuJwt
    val originalJwt: ByuJwt?
}
