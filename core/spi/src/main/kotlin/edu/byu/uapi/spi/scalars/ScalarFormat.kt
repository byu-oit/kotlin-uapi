package edu.byu.uapi.spi.scalars

data class ScalarFormat(
    val type: JsonValueType,
    val format: String? = null
) {
    companion object {
        val INTEGER = ScalarFormat(JsonValueType.NUMBER, OpenApiFormats.INT32)
        val LONG = ScalarFormat(JsonValueType.NUMBER, OpenApiFormats.INT64)
        val FLOAT = ScalarFormat(JsonValueType.NUMBER, OpenApiFormats.FLOAT)
        val DOUBLE = ScalarFormat(JsonValueType.NUMBER, OpenApiFormats.DOUBLE)

        val BOOLEAN = ScalarFormat(JsonValueType.BOOLEAN)

        val STRING = ScalarFormat(JsonValueType.STRING)

        val BYTE_ARRAY = ScalarFormat(JsonValueType.STRING, OpenApiFormats.BYTE)

        val DATE = ScalarFormat(JsonValueType.STRING, JsonSchemaFormats.DATE)
        val DATE_TIME = ScalarFormat(JsonValueType.STRING, JsonSchemaFormats.DATE_TIME)
        val TIME = ScalarFormat(JsonValueType.STRING, JsonSchemaFormats.TIME)

        val UUID = ScalarFormat(JsonValueType.STRING, JsonSchemaFormats.UUID)

        val URI = ScalarFormat(JsonValueType.STRING, JsonSchemaFormats.URI)
    }
}

object JsonSchemaFormats {
    const val DATE = "date"
    const val DATE_TIME = "date-time"
    const val TIME = "time"

    const val EMAIL = "email"
    const val IDN_EMAIL = "idn-email"
    const val HOSTNAME = "hostname"
    const val IDN_HOSTNAME = "idn-hostname"
    const val IPV4 = "ipv4"
    const val IPV6 = "ipv6"

    const val URI = "uri"
    const val URI_REFERENCE = "uri-reference"
    const val IRI = "iri"
    const val IRI_REFERENCE = "iri-reference"
    const val URI_TEMPLATE = "uri-template"

    const val JSON_POINTER = "json-pointer"
    const val RELATIVE_JSON_POINTER = "relative-json-pointer"

    const val REGEX = "regex"
    const val UUID = "uuid"
}

object OpenApiFormats {
    const val INT32 = "int32"
    const val INT64 = "int64"
    const val FLOAT = "float"
    const val DOUBLE = "double"
    const val BYTE = "byte"
}

object OtherFormats {

}

enum class JsonValueType {
    NUMBER, STRING, BOOLEAN
}

