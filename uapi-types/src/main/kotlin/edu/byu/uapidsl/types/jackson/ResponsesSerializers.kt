package edu.byu.uapidsl.types.jackson

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import edu.byu.uapidsl.types.BasicResourceResponse
import edu.byu.uapidsl.types.UAPIField
import edu.byu.uapidsl.types.UAPIMapResource
import edu.byu.uapidsl.types.UAPIResource

val responseMixins = listOf(
    mixin<BasicResourceResponse, BasicResourceReponseMixin>(),
    mixin<UAPIMapResource, UAPIMapResourceMixin>()
)

class BasicResourceReponseMixin {

    @get:JsonAnyGetter
    val fieldsets: Map<String, UAPIResource> = emptyMap()

    @JsonIgnore
    val basic: UAPIResource? = null
}

class UAPIMapResourceMixin {
    @get:JsonAnyGetter
    val properties: Map<String, UAPIField<*>> = emptyMap()
}
