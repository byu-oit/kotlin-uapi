package edu.byu.uapidsl.types

import java.net.URI

enum class ApiType(override val serialized: String) : ApiEnum {
  READ_ONLY("read-only"),
  MODIFIABLE("modifiable"),
  SYSTEM("system"),
  DERIVED("derived"),
  RELATED("related");
}

data class UAPIField<Type>(
  val value: Type,
  val apiType: ApiType,
  val key: Boolean = false,
  val description: String? = null,
  val longDescription: String? = null,
  val displayLabel: String? = null,
  val domain: URI? = null,
  val relatedResource: URI? = null
) {
  companion object {

    fun <Type> prop(
      value: Type,
      apiType: ApiType = ApiType.MODIFIABLE,
      description: String? = null,
      longDescription: String? = null,
      displayLabel: String? = null
    ) = UAPIField(
      value = value,
      apiType = apiType,
      description = description,
      longDescription = longDescription,
      displayLabel = displayLabel,
      key = false,
      domain = null,
      relatedResource = null
    )

    fun <Type> domainProp(
      value: Type,
      apiType: ApiType = ApiType.MODIFIABLE,
      domain: URI,
      description: String? = null,
      longDescription: String? = null,
      displayLabel: String? = null
    ) = UAPIField(
      value = value,
      apiType = apiType,
      description = description,
      longDescription = longDescription,
      displayLabel = displayLabel,
      key = false,
      domain = domain,
      relatedResource = null
    )

    fun <Type> key(
      value: Type,
      apiType: ApiType = ApiType.MODIFIABLE,
      description: String? = null,
      longDescription: String? = null,
      displayLabel: String? = null
    ) = UAPIField(
      value = value,
      apiType = apiType,
      description = description,
      longDescription = longDescription,
      displayLabel = displayLabel,
      key = true,
      domain = null,
      relatedResource = null
    )

    fun <Type> relation(
      value: Type,
      relatedResource: URI,
      description: String? = null,
      longDescription: String? = null,
      displayLabel: String? = null
    ) = UAPIField(
      value = value,
      apiType = ApiType.RELATED,
      description = description,
      longDescription = longDescription,
      displayLabel = displayLabel,
      key = false,
      domain = null,
      relatedResource = relatedResource
    )


  }

}
