package edu.byu.uapidsl.types

import java.time.Instant


interface ResponseMetadata {
    val validationResponse: ValidationResponse
    val validationInformation: List<String>
    val cache: CacheMeta?
}


interface CollectionMetadata: ResponseMetadata {
    val collectionSize: Int
}

data class UAPIResourceMeta(
    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null
) : ResponseMetadata

data class SimpleCollectionMetadata(
    override val collectionSize: Int,

    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null
): CollectionMetadata

data class PagedCollectionMetadata(
    override val collectionSize: Int,
    val pageSize: Int,
    val pageStart: Int,
    val pageEnd: Int,
    val defaultPageSize: Int,
    val maxPageSize: Int,

    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null
): CollectionMetadata

data class ValidationResponse(
    val code: Int = 200,
    val message: String = "OK"
) {
    companion object {
        val OK = ValidationResponse()
    }
}

data class CacheMeta(
    val dateTime: Instant
)
