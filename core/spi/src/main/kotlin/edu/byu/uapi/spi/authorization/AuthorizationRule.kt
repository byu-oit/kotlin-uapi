package edu.byu.uapi.spi.authorization

interface AuthorizationRule<UserContext: Any> {
    fun evaluate(context: UserContext): Boolean
    fun describe(): String
}
