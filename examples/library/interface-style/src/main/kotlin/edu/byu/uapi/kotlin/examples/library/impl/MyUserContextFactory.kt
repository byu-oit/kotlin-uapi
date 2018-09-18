package edu.byu.uapi.kotlin.examples.library.impl

import edu.byu.uapi.server.UserContextAuthnInfo
import edu.byu.uapi.server.UserContextFactory
import edu.byu.uapi.server.UserContextResult

class MyUserContextFactory: UserContextFactory<MyUserContext> {
    override fun createUserContext(authenticationInfo: UserContextAuthnInfo): UserContextResult<MyUserContext> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class MyUserContext {

}
