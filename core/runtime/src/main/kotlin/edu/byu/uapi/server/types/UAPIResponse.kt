package edu.byu.uapi.server.types

import edu.byu.uapi.server.rendering.Renderable
import edu.byu.uapi.server.rendering.Renderer
import edu.byu.uapi.server.rendering.render

sealed class UAPIResponse<MetaType : ResponseMetadata> : Renderable {
    abstract val metadata: MetaType
    abstract val links: UAPILinks

    final override fun render(renderer: Renderer<*>) {
        renderExtras(renderer)
        renderer.tree("links", links)
        renderer.tree("metadata", metadata)
    }

    protected open fun renderExtras(renderer: Renderer<*>) {
    }
}

data class UAPIFieldsetsCollectionResponse(
    val values: List<UAPIFieldsetsResponse>,
    override val metadata: CollectionMetadata,
    override val links: UAPILinks
) : UAPIResponse<CollectionMetadata>() {
    override fun renderExtras(renderer: Renderer<*>) {
        renderer.treeArray("values", values)
    }
}

data class UAPIPropertiesResponse(
    val properties: Map<String, UAPIProperty>,
    override val metadata: UAPIResourceMeta,
    override val links: UAPILinks
) : UAPIResponse<UAPIResourceMeta>() {
    override fun renderExtras(renderer: Renderer<*>) {
        properties.render(renderer)
    }
}

data class UAPIFieldsetsResponse(
    val fieldsets: Map<String, UAPIResponse<*>>,
    override val metadata: FieldsetsMetadata,
    override val links: UAPILinks = emptyMap()
) : UAPIResponse<FieldsetsMetadata>() {
    override fun renderExtras(renderer: Renderer<*>) {
        fieldsets.render(renderer)
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
) : UAPIErrorResponse() {
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
