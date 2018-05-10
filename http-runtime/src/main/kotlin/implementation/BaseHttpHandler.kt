package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectMapper
import edu.byu.jwt.ByuJwt
import edu.byu.jwt.openid.OIDDiscoveryLoaderImpl
import edu.byu.jwt.validate.ByuJwtValidator
import edu.byu.jwt.validate.ByuJwtValidatorImpl
import edu.byu.jwt.validate.JWTValidationException
import edu.byu.uapidsl.AuthContextInput
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.http.*
import edu.byu.uapidsl.types.UAPIResponse

abstract class BaseHttpHandler<Request : HttpRequest, AuthContext : Any>(
    protected val apiModel: UApiModel<AuthContext>,
    protected val jsonMapper: ObjectMapper
) : HttpHandler<Request> {

    private val jwtValidator: ByuJwtValidator = ByuJwtValidatorImpl(setOf(OIDDiscoveryLoaderImpl()))

    private val authContextCreator = apiModel.authContextCreator

    final override fun handle(request: Request): HttpResponse {
        return try {
            val response = handleUAPI(request)

            UAPIHttpResponse(response, jsonMapper)
        } catch (ex: HttpError) {
            ex.printStackTrace()
            ErrorHttpResponse(ex, jsonMapper)
        } catch (ex: Throwable) {
            ex.printStackTrace()
            ErrorHttpResponse(
                HttpError(500, "Unknown Error: ${ex.message}", listOf(
                    "Please try your action again.",
                    "If the problem persists, please contact technical support."
                )), jsonMapper
            )
        }
    }

    private fun handleUAPI(request: Request): UAPIResponse<*> {
        val authContext = getAuthContext(request)

        return handleAuthenticated(request, authContext)
    }

    @Throws(HttpError::class)
    abstract fun handleAuthenticated(request: Request, authContext: AuthContext): UAPIResponse<*>

    private fun getAuthContext(request: Request): AuthContext {
        val input = request.extractAuthContextInput()

        return input.authContextCreator()
    }

    private fun HttpRequest.extractAuthContextInput(): AuthContextInput {
        val jwtHeader = this.headers[ByuJwtValidator.BYU_JWT_HEADER_CURRENT.toLowerCase()]?.firstOrNull()
            ?: throw NoCredentialsException("No authentication information was included in the request", listOf(
                "Make sure that you are calling this API through the BYU API Manager (api.byu.edu)",
                "Check with the developer of the API to ensure that the API is configured properly in the API Manager"
            ))

        val originalJwtHeader = this.headers[ByuJwtValidator.BYU_JWT_HEADER_ORIGINAL.toLowerCase()]?.firstOrNull()

        val jwt: ByuJwt = jwtHeader.decodeAndValidateAsJwt(ByuJwtValidator.BYU_JWT_HEADER_CURRENT)

        val originalJwt = originalJwtHeader?.decodeAndValidateAsJwt(ByuJwtValidator.BYU_JWT_HEADER_ORIGINAL)

        return AuthContextInput(
            this.headers, jwt, originalJwt
        )
    }

    private fun String.decodeAndValidateAsJwt(sourceHeader: String): ByuJwt = try {
        jwtValidator.decodeAndValidateJwt(this)
    } catch (ex: JWTValidationException) {
        throw BadCredentialsException("Unable to validate JWT from header '$sourceHeader'")
    }
}

