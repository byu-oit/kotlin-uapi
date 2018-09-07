package edu.byu.uapi.server.types

import edu.byu.uapi.server.FIELDSET_BASIC

interface UAPIResponse<MetaType : ResponseMetadata> {
    val metadata: MetaType
    val links: UAPILinks
}

data class UAPIPropertiesResponse(
    val properties: Map<String, UAPIProperty<*>>,
    override val metadata: UAPIResourceMeta,
    override val links: UAPILinks
) : UAPIResponse<UAPIResourceMeta>

data class FieldsetsMetadata(
    val fieldSetsReturned: Set<String>,
    val fieldSetsAvailable: Set<String>,
    val fieldSetsDefault: Set<String> = setOf(FIELDSET_BASIC),
    val contextsAvailable: Map<String, Set<String>> = emptyMap(),
    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null

) : ResponseMetadata

data class UAPIFieldsetsResponse(
    val fieldsets: Map<String, UAPIResponse<*>>,
    override val metadata: FieldsetsMetadata,
    override val links: UAPILinks = emptyMap()
) : UAPIResponse<FieldsetsMetadata>
