package edu.byu.uapidsl.types

interface UAPIResponse<MetaType: ResponseMetadata> {
    val metadata: MetaType
    val links: UAPILinks
}

interface UAPIResource: UAPIResponse<UAPIResourceMeta>

interface UAPIResourceResponse {
    val basic: UAPIResource?
    val fieldsets: Map<String, UAPIResource>
}

data class BasicResourceResponse(
    override val fieldsets: Map<String, UAPIResource>
): UAPIResourceResponse {
    override val basic: UAPIResource? = fieldsets["basic"]
}

abstract class UAPIResourceResponseBase(
    override val metadata: UAPIResourceMeta,
    override val links: UAPILinks
): UAPIResponse<UAPIResourceMeta>

data class UAPIMapResource(
    override val metadata: UAPIResourceMeta,
    override val links: UAPILinks,
    val properties: Map<String, UAPIField<*>>
): UAPIResource



