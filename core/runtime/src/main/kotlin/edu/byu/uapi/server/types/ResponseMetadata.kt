package edu.byu.uapi.server.types

import edu.byu.uapi.server.FIELDSET_BASIC
import edu.byu.uapi.server.serialization.TreeSerializationStrategy
import edu.byu.uapi.server.serialization.UAPISerializableTree
import java.time.Instant

sealed class ResponseMetadata: UAPISerializableTree {
    abstract val validationResponse: ValidationResponse
    abstract val validationInformation: List<String>
    abstract val cache: CacheMeta?

    final override fun serialize(strategy: TreeSerializationStrategy) {
        strategy.tree("validation_response", validationResponse)
        strategy.strings("validation_information", validationInformation)
        if (cache != null) {
            strategy.tree("cache", cache)
        }
        serializeExtras(strategy)
    }

    abstract fun serializeExtras(ser: TreeSerializationStrategy)
}

data class UAPIErrorMetadata(
    override val validationResponse: ValidationResponse,
    override val validationInformation: List<String>
) : ResponseMetadata() {
    override fun serializeExtras(ser: TreeSerializationStrategy) {
    }

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
) : ResponseMetadata() {
    override fun serializeExtras(ser: TreeSerializationStrategy) {
        ser.number("collection_size", collectionSize)
        ser.mergeTree(sortMetadata)
        ser.mergeTree(searchMetadata)
        ser.mergeTree(subsetMetadata)
    }
}

data class CollectionSubsetMetadata(
    val subsetSize: Int,
    val subsetStart: Int,
    val defaultSubsetSize: Int,
    val maxSubsetSize: Int
): UAPISerializableTree {
    override fun serialize(strategy: TreeSerializationStrategy) {
        strategy.number("subset_size", subsetSize)
        strategy.number("subset_start", subsetStart)
        strategy.number("default_subset_size", defaultSubsetSize)
        strategy.number("max_subset_size", maxSubsetSize)
    }
}

data class SortableCollectionMetadata(
    val sortPropertiesAvailable: List<String>,
    val sortPropertiesDefault: List<String>,
    val sortOrderDefault: SortOrder
): UAPISerializableTree {
    override fun serialize(strategy: TreeSerializationStrategy) {
        strategy.strings("sort_properties_available", sortPropertiesAvailable)
        strategy.strings("sort_properties_default", sortPropertiesDefault)
        strategy.enum("sort_order_default", sortOrderDefault)
    }
}

data class SearchableCollectionMetadata(
    val searchContextsAvailable: Map<String, Collection<String>>
): UAPISerializableTree {
    override fun serialize(strategy: TreeSerializationStrategy) {
        strategy.tree("search_contexts_available") {
            searchContextsAvailable.entries.forEach { e ->
                strings(e.key, e.value)
            }
        }
    }
}

data class UAPIResourceMeta(
    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null
) : ResponseMetadata() {
    override fun serializeExtras(ser: TreeSerializationStrategy) {
    }
}

data class ValidationResponse(
    val code: Int = 200,
    val message: String = "OK"
): UAPISerializableTree {
    override fun serialize(strategy: TreeSerializationStrategy) {
        strategy.number("code", code)
        strategy.string("message", message)
    }

    companion object {
        val OK = ValidationResponse()
    }

    //    @JsonIgnore
    val successful = code in 200..299
}

data class CacheMeta(
    val dateTime: Instant
): UAPISerializableTree {
    override fun serialize(strategy: TreeSerializationStrategy) {
        strategy.string("date_time", dateTime.toString())
    }
}

data class FieldsetsMetadata(
    val fieldSetsReturned: Set<String>,
    val fieldSetsAvailable: Set<String>,
    val fieldSetsDefault: Set<String> = setOf(FIELDSET_BASIC),
    val contextsAvailable: Map<String, Set<String>> = emptyMap(),
    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null

) : ResponseMetadata() {
    override fun serializeExtras(ser: TreeSerializationStrategy) {
        ser.strings("field_sets_returned", fieldSetsReturned)
        ser.strings("field_sets_available", fieldSetsAvailable)
        ser.strings("field_sets_default", fieldSetsDefault)
        ser.tree("contexts_available") {
            contextsAvailable.forEach { k, v -> ser.strings(k, v) }
        }
    }
}

