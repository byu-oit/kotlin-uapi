package edu.byu.uapi.http

import edu.byu.uapi.server.types.*
import javax.json.Json
import javax.json.JsonObject
import javax.json.JsonObjectBuilder

const val JSON_METADATA = "metadata"
const val JSON_LINKS = "links"

fun UAPIResponse<*>.toJson(): JsonObject {
    val json = Json.createObjectBuilder()
        .add(JSON_METADATA, metadata.toJson())
        .add(JSON_LINKS, links.toJson())

    when (this) {
        is UAPIErrorResponse -> this.addToJson(json)
        is UAPIFieldsetsCollectionResponse -> TODO()
        is UAPIPropertiesResponse -> TODO()
        is UAPIFieldsetsResponse -> TODO()
    }

    return json.build()
}

internal fun UAPIErrorResponse.addToJson(json: JsonObjectBuilder) {
    // Placeholder. Nothing extra to add.
}

internal fun UAPIPropertiesResponse.addToJson(json: JsonObjectBuilder) {

}


fun JsonObjectBuilder.addMetadataFields(prop: UAPIProperty<*>): JsonObjectBuilder {
    add("api_type", prop.apiType.toJson())
    if (prop.key) {
        add("key", prop.key)
    }
    maybeAdd("description", prop.description)
    maybeAdd("long_description", prop.longDescription)
    val disp = prop.displayLabel
    if (disp != null) {
        add("display_label", disp)
    }
    maybeAdd("domain", prop.domain)
    maybeAdd("related_resource", prop.relatedResource)

    return this
}

fun JsonObjectBuilder.maybeAdd(key: String, value: OrMissing<String>): JsonObjectBuilder {
    when(value) {
        is OrMissing.Present -> {
            val v = value.value
            if (v == null) {
                addNull(key)
            } else {
                add(key, v)
            }
        }
    }
    return this
}

fun UAPILink.toJson(): JsonObject =
    Json.createObjectBuilder()
        .add("rel", this.rel)
        .add("href", this.href)
        .add("method", this.method.name)
        .build()

fun UAPILinks.toJson(): JsonObject = this.entries.fold(Json.createObjectBuilder()) { j, it ->
    j.add(it.key, it.value.toJson())
}.build()

const val JSON_VALIDATION_RESPONSE = "validation_response"
const val JSON_VALIDATION_INFORMATION = "validation_information"
const val JSON_CACHE = "cache"
const val JSON_CACHE_DATE_TIME = "date_time"

const val JSON_COLLECTION_SIZE = "collection_size"

const val JSON_SORT_PROPS_AVAILABLE = "sort_properties_available"
const val JSON_SORT_PROPS_DEFAULT = "sort_properties_default"
const val JSON_SORT_ORDER_DEFAULT = "sort_order_default"

const val JSON_SEARCH_CONTEXTS_AVAILABLE = "search_contexts_available"

const val JSON_SUBSET_SIZE = "subset_size"
const val JSON_SUBSET_START = "subset_start"
const val JSON_DEFAULT_SUBSET_SIZE = "default_subset_size"
const val JSON_MAX_SUBSET_SIZE = "max_subset_size"

const val JSON_RESP_CODE = "code"
const val JSON_RESP_MESSAGE = "message"

fun ResponseMetadata.toJson(): JsonObject {
    return Json.createObjectBuilder().let {
        it.add(JSON_VALIDATION_RESPONSE, validationResponse.toJson())
        it.add(JSON_VALIDATION_INFORMATION, Json.createArrayBuilder(validationInformation))
        val cache = this.cache
        if (cache != null) {
            it.add(JSON_CACHE, cache.toJson())
        }

        if (this is CollectionMetadata) {
            it.add(JSON_COLLECTION_SIZE, this.collectionSize)

            val sort = this.sortMetadata
            val search = this.searchMetadata
            val subset = this.subsetMetadata

            if (sort != null) {
                it.add(JSON_SORT_PROPS_AVAILABLE, sort.sortPropertiesAvailable)
                    .add(JSON_SORT_PROPS_DEFAULT, sort.sortPropertiesDefault)
                    .add(JSON_SORT_ORDER_DEFAULT, sort.sortOrderDefault.toJson())
            }

            if (search != null) {
                it.add(JSON_SEARCH_CONTEXTS_AVAILABLE, search.searchContextsAvailable.asIterable().fold(Json.createObjectBuilder()) { json, ctx ->
                    json.add(ctx.key, ctx.value)
                })
            }

            if (subset != null) {
                it.add(JSON_SUBSET_SIZE, subset.subsetSize)
                    .add(JSON_SUBSET_START, subset.subsetStart)
                    .add(JSON_DEFAULT_SUBSET_SIZE, subset.defaultSubsetSize)
                    .add(JSON_MAX_SUBSET_SIZE, subset.maxSubsetSize)
            }
        }

        it.build()
    }
}

fun CacheMeta.toJson() = Json.createObjectBuilder()
    .add(JSON_CACHE_DATE_TIME, this.dateTime.toString())
    .build()

fun ValidationResponse.toJson() = Json.createObjectBuilder()
    .add(JSON_RESP_CODE, this.code)
    .add(JSON_RESP_MESSAGE, this.message)
    .build()

fun JsonObjectBuilder.add(
    key: String,
    array: Collection<*>
): JsonObjectBuilder {
    this.add(key, Json.createArrayBuilder(array))
    return this
}

fun <E : Enum<E>> E.toJson(): String = this.name.toLowerCase()
