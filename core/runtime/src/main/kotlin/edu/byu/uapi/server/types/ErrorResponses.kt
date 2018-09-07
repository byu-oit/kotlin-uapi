package edu.byu.uapi.server.types


data class ErrorResponse(
    override val metadata: UAPIErrorMetadata
) : UAPIResponse<UAPIErrorMetadata> {
    override val links: UAPILinks = emptyMap()

    companion object {
        fun notAuthorized() = UAPINotAuthorizedError

        fun notFound() = UAPINotFoundError

    }
}

class UAPINotAuthenticatedError(
    messages: List<String>
): UAPIResponse<UAPIErrorMetadata> {

    override val metadata: UAPIErrorMetadata = UAPIErrorMetadata(
        ValidationResponse(
            403,
            "Unauthorized"
        ),
        validationInformation = messages
    )
    override val links: UAPILinks = emptyMap()

}

object UAPINotAuthorizedError: UAPIResponse<UAPIErrorMetadata> {
    override val metadata: UAPIErrorMetadata = UAPIErrorMetadata(
        ValidationResponse(403, "Not Authorized"),
        listOf("The caller is not authorized to perform the requested action")
    )
    override val links: UAPILinks = emptyMap()
}

object UAPINotFoundError: UAPIResponse<UAPIErrorMetadata> {
    override val metadata: UAPIErrorMetadata = UAPIErrorMetadata(
        ValidationResponse(404, "Not Found"),
        listOf("Not Found")
    )
    override val links: UAPILinks = emptyMap()
}

object UAPIOperationNotImplementedError: UAPIResponse<UAPIErrorMetadata> {
    override val metadata: UAPIErrorMetadata = UAPIErrorMetadata(
        ValidationResponse(404, "Not Found"),
        listOf("Not Found")
    )
    override val links: UAPILinks = emptyMap()
}
