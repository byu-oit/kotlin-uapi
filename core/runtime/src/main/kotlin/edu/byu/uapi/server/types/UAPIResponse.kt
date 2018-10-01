package edu.byu.uapi.server.types

import edu.byu.uapi.server.serialization.TreeSerializationStrategy
import edu.byu.uapi.server.serialization.UAPISerializableTree

sealed class UAPIResponse<MetaType : ResponseMetadata>: UAPISerializableTree {
    abstract val metadata: MetaType
    abstract val links: UAPILinks

    final override fun serialize(strategy: TreeSerializationStrategy) {
        serializeExtras(strategy)
        strategy.tree("links", links)
        strategy.tree("metadata", metadata)
    }

    protected open fun serializeExtras(ser: TreeSerializationStrategy) {
    }
}

data class UAPIFieldsetsCollectionResponse(
    val values: List<UAPIFieldsetsResponse>,
    override val metadata: CollectionMetadata,
    override val links: UAPILinks
): UAPIResponse<CollectionMetadata>() {
    override fun serializeExtras(ser: TreeSerializationStrategy) {
        ser.trees("values", values)
    }
}

data class UAPIPropertiesResponse(
    val properties: Map<String, UAPIProperty>,
    override val metadata: UAPIResourceMeta,
    override val links: UAPILinks
) : UAPIResponse<UAPIResourceMeta>() {
    override fun serializeExtras(ser: TreeSerializationStrategy) {
        ser.mergeTree(this.properties)
    }
}

data class UAPIFieldsetsResponse(
    val fieldsets: Map<String, UAPIResponse<*>>,
    override val metadata: FieldsetsMetadata,
    override val links: UAPILinks = emptyMap()
) : UAPIResponse<FieldsetsMetadata>() {
    override fun serializeExtras(ser: TreeSerializationStrategy) {
        ser.mergeTree(this.fieldsets)
    }
}

sealed class UAPIErrorResponse : UAPIResponse<UAPIErrorMetadata>() {
    companion object {
        fun notAuthorized() = UAPINotAuthorizedError

        fun notFound() = UAPINotFoundError
    }
}

data class GenericUAPIErrorResponse(
    val statusCode: Int,
    val message: String,
    val validationInformation: List<String>
): UAPIErrorResponse() {
    override val metadata: UAPIErrorMetadata = UAPIErrorMetadata(
        ValidationResponse(statusCode, message),
        validationInformation
    )
    override val links: UAPILinks = emptyMap()
}

class UAPINotAuthenticatedError(
    messages: List<String>
) : UAPIErrorResponse() {

    override val metadata: UAPIErrorMetadata = UAPIErrorMetadata(
        ValidationResponse(
            403,
            "Unauthorized"
        ),
        validationInformation = messages
    )
    override val links: UAPILinks = emptyMap()

}

object UAPINotAuthorizedError : UAPIErrorResponse() {
    override val metadata: UAPIErrorMetadata = UAPIErrorMetadata(
        ValidationResponse(403, "Not Authorized"),
        listOf("The caller is not authorized to perform the requested action")
    )
    override val links: UAPILinks = emptyMap()
}

object UAPINotFoundError : UAPIErrorResponse() {
    override val metadata: UAPIErrorMetadata = UAPIErrorMetadata(
        ValidationResponse(404, "Not Found"),
        listOf("Not Found")
    )
    override val links: UAPILinks = emptyMap()
}

object UAPIOperationNotImplementedError : UAPIErrorResponse() {
    override val metadata: UAPIErrorMetadata = UAPIErrorMetadata(
        ValidationResponse(404, "Not Found"),
        listOf("Not Found")
    )
    override val links: UAPILinks = emptyMap()
}
