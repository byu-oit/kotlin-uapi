package edu.byu.uapi.spi.authorization

interface AuthorizationHandler<UserContext: Any, Context: Any> {
    fun check(userContext: UserContext, context: Context): Boolean
    fun describe(): String
}
