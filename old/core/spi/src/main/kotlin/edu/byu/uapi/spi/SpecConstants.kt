package edu.byu.uapi.spi

object SpecConstants {
    object Metadata {
        const val KEY = "metadata"

        object ValidationResponse {
            const val KEY = "validation_response"
            const val KEY_CODE = "code"
            const val KEY_MESSAGE = "message"
        }

        const val KEY_VALIDATION_INFORMATION = "validation_information"

        object Cache {
            const val KEY = "cache"
            const val KEY_DATE_TIME = "date_time"
        }

        const val KEY_RESTRICTED = "restricted"
    }

    object Links {
        const val KEY = "links"
        const val KEY_REL = "rel"

        const val VALUE_REL__SELF = "self"

        const val KEY_HREF = "href"
        const val KEY_METHOD = "method"
    }

    object Properties {
        const val KEY_VALUE = "value"
        const val KEY_VALUE_ARRAY = "value_array"
        const val KEY_OBJECT = "object"
        const val KEY_OBJECT_ARRAY = "object_array"

        const val KEY_API_TYPE = "api_type"
        const val KEY_KEY = "key"
        const val KEY_DESCRIPTION = "description"
        const val KEY_LONG_DESCRIPTION = "long_description"
        const val KEY_DISPLAY_LABEL = "display_label"
        const val KEY_DOMAIN = "domain"
        const val KEY_RELATED_RESOURCE = "related_resource"
    }


    object Collections {
        object Metadata {
            const val KEY_COLLECTION_SIZE = "collection_size"

            const val KEY_SORT_PROPERTIES_AVAILABLE = "sort_properties_available"
            const val KEY_SORT_PROPERTIES_DEFAULT = "sort_properties_default"
            const val KEY_SORT_ORDER_DEFAULT = "sort_order_default"

            const val KEY_SUBSET_DEFAULT_SIZE = "default_subset_size"
            const val KEY_SUBSET_MAX_SIZE = "max_subset_size"
            const val KEY_SUBSET_START = "subset_start"
            const val KEY_SUBSET_SIZE = "subset_size"

            const val KEY_SEARCH_CONTEXTS_AVAILABLE = "search_contexts_available"
        }

        object Links {
            const val SUFFIX_INFO = "__info"
            const val SUFFIX_FIRST = "__first"
            const val SUFFIX_CURRENT = "__current"
            const val SUFFIX_LAST = "__last"
            const val SUFFIX_PREVIOUS = "__previous"
            const val SUFFIX_NEXT = "__next"
        }

        object Response {
            const val KEY_VALUES = "values"
        }

        object Query {
            const val KEY_SORT_PROPERTIES = "sort_properties"
            const val KEY_SORT_ORDER = "sort_order"

            const val KEY_SUBSET_START_OFFSET = "subset_start_offset"
            const val KEY_SUBSET_SIZE = "subset_size"
            const val KEY_SUBSET_START_KEY = "subset_start_key"
            const val VALUE_DEFAULT_START_OFFSET = 0

            const val KEY_SEARCH_CONTEXT = "search_context"
            const val KEY_SEARCH_TEXT = "search_text"
        }
    }

    object FieldSets {
        const val VALUE_BASIC = "basic"

        val DEFAULT_FIELDSETS: Set<String> = java.util.Collections.singleton(VALUE_BASIC)

        object Metadata {
            const val KEY_FIELD_SETS_RETURNED = "field_sets_returned"
            const val KEY_FIELD_SETS_AVAILABLE = "field_sets_available"
            const val KEY_FIELD_SETS_DEFAULT = "field_sets_default"

            const val KEY_CONTEXTS_AVAILABLE = "contexts_available"
        }

        object Query {
            const val KEY_FIELD_SETS = "field_sets"

            const val KEY_CONTEXTS = "contexts"
        }

    }

    object Files {
        const val MIME_METADATA = "application/vnd.byu.uapi+json"
    }
}
