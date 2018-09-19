package edu.byu.uapi.library

import edu.byu.jwt.ByuJwt
import edu.byu.uapi.kotlin.examples.library.Library
import edu.byu.uapi.server.UserContextAuthnInfo
import edu.byu.uapi.server.UserContextResult
import edu.byu.uapi.utilities.jwt.JwtUserContextFactory

class LibraryUser(
  val netId: String,
  val cardholderId: Int?,
  val isAdmin: Boolean
) {
    val isCardholder = cardholderId != null
}

private val adminNetIds = setOf("{your NetId here}")

class LibraryUserContextFactory: JwtUserContextFactory<LibraryUser>() {
    override fun createUserContext(
        authenticationInfo: UserContextAuthnInfo,
        currentJwt: ByuJwt,
        originalJwt: ByuJwt?
    ): UserContextResult<LibraryUser> {
        val netId = currentJwt.resourceOwnerClaims?.netId ?: currentJwt.clientClaims.netId

        if (netId == null) {
            return UserContextResult.Failure("No NetID was provided.")
        }

        val cardholderId = Library.getCardholderIdForNetId(netId)

        return UserContextResult.Success(LibraryUser(
            netId = netId,
            cardholderId = cardholderId,
            isAdmin = adminNetIds.contains(netId)
        ))
    }
}
