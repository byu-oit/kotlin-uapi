package edu.byu.uapi.server.types

interface UAPIResponse<MetaType : ResponseMetadata> {
    val metadata: MetaType
    val links: UAPILinks
}


data class UAPIPropertiesResponse<PropertiesType>(
    val properties: PropertiesType,
    override val metadata: UAPIResourceMeta,
    override val links: UAPILinks
): UAPIResponse<UAPIResourceMeta>
