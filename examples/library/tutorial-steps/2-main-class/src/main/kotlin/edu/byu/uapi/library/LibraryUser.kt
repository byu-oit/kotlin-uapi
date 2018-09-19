package edu.byu.uapi.library

import edu.byu.uapi.server.UserContextAuthnInfo
import edu.byu.uapi.server.UserContextFactory
import edu.byu.uapi.server.UserContextResult

class LibraryUser

class LibraryUserContextFactory: UserContextFactory<LibraryUser> {
    override fun createUserContext(authenticationInfo: UserContextAuthnInfo): UserContextResult<LibraryUser> {
        TODO("not implemented")
    }
}
