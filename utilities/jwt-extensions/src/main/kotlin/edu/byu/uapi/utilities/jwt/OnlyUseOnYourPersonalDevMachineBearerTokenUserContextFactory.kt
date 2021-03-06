package edu.byu.uapi.utilities.jwt

import edu.byu.jwt.validate.ByuJwtValidator
import edu.byu.uapi.server.UserContextAuthnInfo
import edu.byu.uapi.server.UserContextFactory
import edu.byu.uapi.server.UserContextResult
import edu.byu.uapi.spi.requests.Headers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

val DEFAULT_USER_INFO_SERVICE = URL("https://api.byu.edu/openid-userinfo/v1/userinfo")

class OnlyUseOnYourPersonalDevMachineBearerTokenUserContextFactory<UserContext : Any>(
    private val wrapped: UserContextFactory<UserContext>,
    private val userInfoServiceUrl: URL = DEFAULT_USER_INFO_SERVICE
) : UserContextFactory<UserContext> {
    init {
        LOG.warn("This class is intended for use in development environments, and should never be used in production.")
    }

    override fun createUserContext(authenticationInfo: UserContextAuthnInfo): UserContextResult<UserContext> {
        LOG.debug("Creating user contexts")
        val authnInfo = maybeDecorateAuthInfo(authenticationInfo)
        return when (authnInfo) {
            is UserContextResult.Success -> wrapped.createUserContext(authnInfo.result)
            is UserContextResult.Failure -> authnInfo
        }
    }

    companion object {
        internal val LOG: Logger = LoggerFactory.getLogger(OnlyUseOnYourPersonalDevMachineBearerTokenUserContextFactory::class.java)
    }

    private fun maybeDecorateAuthInfo(authInfo: UserContextAuthnInfo): UserContextResult<UserContextAuthnInfo> {
        val auth: String? = authInfo.headers["Authorization"]?.firstOrNull()

        if (auth == null || !auth.startsWith("Bearer ")) {
            LOG.debug("No bearer token; not decorating")
            return UserContextResult.Success(authInfo)
        }

        LOG.debug("Calling userinfo service with bearer token")
        val (code, body) = callUrl(userInfoServiceUrl, auth)

        LOG.debug("Got response code $code")
        return when (code) {
            401 -> UserContextResult.Failure("Invalid bearer token in 'Authorization' header.")
            403 -> handleUnauthorized(body)
            200 -> {
                UserContextResult.Success(ExtraHeaderUserContextAuthnInfo(authInfo, mapOf(
                    ByuJwtValidator.BYU_JWT_HEADER_CURRENT to setOf(body)
                )))
            }
            else -> {
                LOG.error("Unrecognized token validation error: HTTP $code\n-------------------\n $body \n-------------------\n")
                UserContextResult.Failure("Error validating bearer token: HTTP $code. See server log for details.")
            }
        }
    }
}

private val WSO2_ERROR_PATTERN = """<(?:\w+:)code>(\d+)</(?:\w+:)code>""".toRegex()

private const val WSO2_NOT_SUBSCRIBED_ERROR = "900908"

private fun handleUnauthorized(body: String): UserContextResult.Failure {
    val match = WSO2_ERROR_PATTERN.find(body)
    if (match == null) {
        OnlyUseOnYourPersonalDevMachineBearerTokenUserContextFactory.LOG.error("Unexpected WSO2 Error response body for HTTP 403:\n-------------------\n $body \n-------------------\n")
        return UserContextResult.Failure("Validation of 'Authorization' header failed with HTTP 403 and an unrecognized response body. See server log for details.")
    }
    val code = match.groupValues[1]
    if (code == WSO2_NOT_SUBSCRIBED_ERROR) {
        return UserContextResult.Failure("In development mode, your credentials must be subscribed to the UserInfo API: https://api.byu.edu/store/apis/info?name=OpenID-Userinfo&version=v1&provider=BYU/jmooreoa")
    }
    OnlyUseOnYourPersonalDevMachineBearerTokenUserContextFactory.LOG.error("Unexpected WSO2 Error response code for HTTP 403:\n-------------------\n $body \n-------------------\n")
    return UserContextResult.Failure("Validation of 'Authorization' failed with WSO2 Error code $code. See server log for details.")
}

private fun callUrl(
    url: URL,
    authHeader: String
): Pair<Int, String> {
    val actualUrl = URL(url.toString() + "?schema=openid")
    return actualUrl.openConnection().use {
        addRequestProperty("Authorization", authHeader)
        addRequestProperty("Accept", "application/jwt")

        connectTimeout = 1000 //ms
        allowUserInteraction = true //Stops weird behaviors on 401 or 407 response
        doInput = true

        connect()

        val code = responseCode

        val stream: InputStream = if (code == 200) inputStream else errorStream ?: inputStream

        responseCode to stream.bufferedReader().use { it.readText() }
    }
}

private inline fun <R> URLConnection.use(fn: HttpURLConnection.() -> R): R {
    this as HttpURLConnection
    try {
        return this.fn()
    } finally {
        this.disconnect()
    }
}

private class ExtraHeaderUserContextAuthnInfo(
    val wrapped: UserContextAuthnInfo,
    extraHeaders: Map<String, Set<String>>
) : UserContextAuthnInfo by wrapped {

    override val headers = ExtraHeaders(wrapped.headers, extraHeaders)
}

private class ExtraHeaders(
    val wrapped: Headers,
    extras: Map<String, Set<String>>
) : Headers {
    private val extraMap = extras.mapKeys { it.key.toLowerCase() }
    override fun get(header: String): Set<String> {
        return extraMap.getOrElse(header.toLowerCase()) { wrapped[header] }
    }
}

