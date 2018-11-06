package edu.byu.uapi.server.types

import edu.byu.uapi.spi.SpecConstants
import edu.byu.uapi.spi.SpecConstants.Metadata
import edu.byu.uapi.spi.input.SortOrder
import edu.byu.uapi.spi.rendering.Renderable
import edu.byu.uapi.spi.rendering.Renderer
import java.time.Instant
import kotlin.Int
import kotlin.String
import kotlin.let
import edu.byu.uapi.spi.SpecConstants.Collections.Metadata as CollectionMeta
import edu.byu.uapi.spi.SpecConstants.FieldSets.Metadata as FieldSetMeta

sealed class ResponseMetadata : Renderable {
    abstract val validationResponse: ValidationResponse
    abstract val validationInformation: List<String>
    abstract val cache: CacheMeta?

    final override fun render(renderer: Renderer<*>) {
        renderer.tree(Metadata.ValidationResponse.KEY, validationResponse)
        if (validationInformation.isNotEmpty()) {
            renderer.valueArray(Metadata.KEY_VALIDATION_INFORMATION, validationInformation)
        }
        cache?.let { renderer.tree(Metadata.Cache.KEY, it) }
        renderExtras(renderer)
    }

    abstract fun renderExtras(renderer: Renderer<*>)
}

data class UAPIErrorMetadata(
    override val validationResponse: ValidationResponse,
    override val validationInformation: List<String>
) : ResponseMetadata() {
    override fun renderExtras(renderer: Renderer<*>) {
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
    override fun renderExtras(renderer: Renderer<*>) {
        renderer.value(CollectionMeta.KEY_COLLECTION_SIZE, collectionSize)
        sortMetadata?.render(renderer)
        searchMetadata?.render(renderer)
        subsetMetadata?.render(renderer)
    }
}

data class CollectionSubsetMetadata(
    val subsetSize: Int,
    val subsetStart: Int,
    val defaultSubsetSize: Int,
    val maxSubsetSize: Int
) : Renderable {
    override fun render(renderer: Renderer<*>) {
        renderer.value(CollectionMeta.KEY_SUBSET_SIZE, subsetSize)
        renderer.value(CollectionMeta.KEY_SUBSET_START, subsetStart)
        renderer.value(CollectionMeta.KEY_SUBSET_DEFAULT_SIZE, defaultSubsetSize)
        renderer.value(CollectionMeta.KEY_SUBSET_MAX_SIZE, maxSubsetSize)
    }
}

data class SortableCollectionMetadata(
    val sortPropertiesAvailable: List<String>,
    val sortPropertiesDefault: List<String>,
    val sortOrderDefault: SortOrder
) : Renderable {
    override fun render(renderer: Renderer<*>) {
        renderer.value(CollectionMeta.KEY_SORT_PROPERTIES_AVAILABLE, sortPropertiesAvailable)
        renderer.value(CollectionMeta.KEY_SORT_PROPERTIES_DEFAULT, sortPropertiesDefault)
        renderer.value(CollectionMeta.KEY_SORT_ORDER_DEFAULT, sortOrderDefault)
    }
}

data class SearchableCollectionMetadata(
    val searchContextsAvailable: Map<String, Collection<String>>
) : Renderable {
    override fun render(renderer: Renderer<*>) {
        renderer.tree(CollectionMeta.KEY_SEARCH_CONTEXTS_AVAILABLE) {
            searchContextsAvailable.entries.forEach { e ->
                valueArray(e.key, e.value)
            }
        }
    }
}

data class UAPIResourceMeta(
    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null
) : ResponseMetadata() {
    override fun renderExtras(renderer: Renderer<*>) {
    }
}

data class ValidationResponse(
    val code: Int = 200,
    val message: String = "OK"
) : Renderable {
    override fun render(renderer: Renderer<*>) {
        renderer.value(Metadata.ValidationResponse.KEY_CODE, code)
        renderer.value(Metadata.ValidationResponse.KEY_MESSAGE, message)
    }

    companion object {
        val OK = ValidationResponse()
    }

    //    @JsonIgnore
    val successful = code in 200..299
}

data class CacheMeta(
    val dateTime: Instant
) : Renderable {
    override fun render(renderer: Renderer<*>) {
        renderer.value(Metadata.Cache.KEY_DATE_TIME, dateTime)
    }
}

data class FieldsetsMetadata(
    val fieldSetsReturned: Set<String>,
    val fieldSetsAvailable: Set<String>,
    val fieldSetsDefault: Set<String> = setOf(SpecConstants.FieldSets.VALUE_BASIC),
    val contextsAvailable: Map<String, Set<String>> = emptyMap(),
    override val validationResponse: ValidationResponse = ValidationResponse.OK,
    override val validationInformation: List<String> = emptyList(),
    override val cache: CacheMeta? = null

) : ResponseMetadata() {
    override fun renderExtras(renderer: Renderer<*>) {
        renderer.valueArray(FieldSetMeta.KEY_FIELD_SETS_RETURNED, fieldSetsReturned)
        renderer.valueArray(FieldSetMeta.KEY_FIELD_SETS_AVAILABLE, fieldSetsAvailable)
        renderer.valueArray(FieldSetMeta.KEY_FIELD_SETS_DEFAULT, fieldSetsDefault)
        renderer.tree(FieldSetMeta.KEY_CONTEXTS_AVAILABLE) {
            contextsAvailable.forEach { k, v -> renderer.valueArray(k, v) }
        }
    }
}

