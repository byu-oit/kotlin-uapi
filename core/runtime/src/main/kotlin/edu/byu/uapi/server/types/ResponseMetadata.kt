package edu.byu.uapi.server.types

import edu.byu.uapi.server.FIELDSET_BASIC
import edu.byu.uapi.server.rendering.Renderable
import edu.byu.uapi.server.rendering.Renderer
import java.time.Instant

sealed class ResponseMetadata : Renderable {
    abstract val validationResponse: ValidationResponse
    abstract val validationInformation: List<String>
    abstract val cache: CacheMeta?

    final override fun render(renderer: Renderer<*>) {
        renderer.tree("validation_response", validationResponse)
        if (validationInformation.isNotEmpty()) {
            renderer.valueArray("validation_information", validationInformation)
        }
        cache?.let { renderer.tree("cache", it) }
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
        renderer.value("collection_size", collectionSize)
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
        renderer.value("subset_size", subsetSize)
        renderer.value("subset_start", subsetStart)
        renderer.value("default_subset_size", defaultSubsetSize)
        renderer.value("max_subset_size", maxSubsetSize)
    }
}

data class SortableCollectionMetadata(
    val sortPropertiesAvailable: List<String>,
    val sortPropertiesDefault: List<String>,
    val sortOrderDefault: SortOrder
) : Renderable {
    override fun render(renderer: Renderer<*>) {
        renderer.value("sort_properties_available", sortPropertiesAvailable)
        renderer.value("sort_properties_default", sortPropertiesDefault)
        renderer.value("sort_order_default", sortOrderDefault)
    }
}

data class SearchableCollectionMetadata(
    val searchContextsAvailable: Map<String, Collection<String>>
) : Renderable {
    override fun render(renderer: Renderer<*>) {
        renderer.tree("search_contexts_available") {
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
        renderer.value("code", code)
        renderer.value("message", message)
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
        renderer.value("date_time", dateTime)
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
    override fun renderExtras(renderer: Renderer<*>) {
        renderer.valueArray("field_sets_returned", fieldSetsReturned)
        renderer.valueArray("field_sets_available", fieldSetsAvailable)
        renderer.valueArray("field_sets_default", fieldSetsDefault)
        renderer.tree("contexts_available") {
            contextsAvailable.forEach { k, v -> renderer.valueArray(k, v) }
        }
    }
}

