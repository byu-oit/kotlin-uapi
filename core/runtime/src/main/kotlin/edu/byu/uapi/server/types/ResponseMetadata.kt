package edu.byu.uapi.server.types

import edu.byu.uapi.server.FIELDSET_BASIC
import java.time.Instant

sealed class ResponseMetadata: UAPISerializable {
    abstract val validationResponse: ValidationResponse
    abstract val validationInformation: List<String>
    abstract val cache: CacheMeta?

    override fun serialize(strategy: SerializationStrategy) {
        TODO("not implemented")
    }
}

data class UAPIErrorMetadata(
    override val validationResponse: ValidationResponse,
    override val validationInformation: List<String>
) : ResponseMetadata() {
    override val cache: CacheMeta? = null
}

data class CollectionMetadata(
    val collectionSize: Int,
    val sortMetadata: SortableCollectionMetadata? = null,
    val searchMetadata: SearchableCollectionMetadata? = null,
    val subsetMetadata: CollectionSubsetMetadata? = null,
    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null
) : ResponseMetadata()

data class CollectionSubsetMetadata(
    val subsetSize: Int,
    val subsetStart: Int,
    val defaultSubsetSize: Int,
    val maxSubsetSize: Int
)

data class SortableCollectionMetadata(
    val sortPropertiesAvailable: List<String>,
    val sortPropertiesDefault: List<String>,
    val sortOrderDefault: SortOrder
)

data class SearchableCollectionMetadata(
    val searchContextsAvailable: Map<String, Collection<String>>
)

data class UAPIResourceMeta(
    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null
) : ResponseMetadata()

data class ValidationResponse(
    val code: Int = 200,
    val message: String = "OK"
) {
    companion object {
        val OK = ValidationResponse()
    }

    //    @JsonIgnore
    val successful = code in 200..299
}

data class CacheMeta(
    val dateTime: Instant
)

data class FieldsetsMetadata(
    val fieldSetsReturned: Set<String>,
    val fieldSetsAvailable: Set<String>,
    val fieldSetsDefault: Set<String> = setOf(FIELDSET_BASIC),
    val contextsAvailable: Map<String, Set<String>> = emptyMap(),
    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null

) : ResponseMetadata()

