package edu.byu.uapi.utilities.jwt

import edu.byu.jwt.ByuJwt
import edu.byu.jwt.openid.OIDDiscoveryLoader
import edu.byu.jwt.validate.ByuJwtValidator
import edu.byu.jwt.validate.ByuJwtValidatorImpl
import edu.byu.jwt.validate.JWTValidationException
import edu.byu.uapi.server.UserContextAuthnInfo
import edu.byu.uapi.server.UserContextFactory
import edu.byu.uapi.server.UserContextResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

abstract class JwtUserContextFactory<UserContext : Any>(
    private val validator: ByuJwtValidator = defaultJwtValidator()
) : UserContextFactory<UserContext> {

    override fun createUserContext(authenticationInfo: UserContextAuthnInfo): UserContextResult<UserContext> {
        val jwtHeader = authenticationInfo.headers[ByuJwtValidator.BYU_JWT_HEADER_CURRENT.toLowerCase()]?.firstOrNull()
        val originalHeader = authenticationInfo.headers[ByuJwtValidator.BYU_JWT_HEADER_ORIGINAL.toLowerCase()]?.firstOrNull()

        if (jwtHeader == null) {
            LOG.warn("No current JWT found in headers")
            return UserContextResult.Failure("Request has no JWT credentials (expected in header '${ByuJwtValidator.BYU_JWT_HEADER_CURRENT}')")
        }
        val currentJwt = try {
            validator.decodeAndValidateJwt(jwtHeader)
        } catch (ex: JWTValidationException) {
            LOG.error("Current JWT validation failed", ex)
            return UserContextResult.Failure("The JWT passed in '${ByuJwtValidator.BYU_JWT_HEADER_CURRENT}' is invalid. Check that is has not expired and has not been tampered with.")
        }

        val originalJwt = if (originalHeader != null) {
            try {
                validator.decodeAndValidateJwt(jwtHeader)
            } catch (ex: JWTValidationException) {
                LOG.error("Original JWT validation failed", ex)
                return UserContextResult.Failure("The JWT passed in '${ByuJwtValidator.BYU_JWT_HEADER_ORIGINAL}' is invalid. Check that is has not expired and has not been tampered with.")
            }
        } else null

        return createUserContext(authenticationInfo, currentJwt, originalJwt)
    }

    abstract fun createUserContext(
        authenticationInfo: UserContextAuthnInfo,
        currentJwt: ByuJwt,
        originalJwt: ByuJwt?
    ): UserContextResult<UserContext>

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(JwtUserContextFactory::class.java)

        private fun defaultJwtValidator() = ByuJwtValidatorImpl(Collections.singleton(
            OIDDiscoveryLoader.Builder()
                .defaultCacheDuration(300_000 /* 5 minutes */)
                .build()
        ))
    }
}
