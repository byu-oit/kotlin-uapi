package edu.byu.uapidsl.http.implementation

import edu.byu.uapidsl.http.HttpRequest
import edu.byu.uapidsl.model.resource.identified.IdentifiedResource
import edu.byu.uapidsl.types.LinkMethod
import edu.byu.uapidsl.types.UAPILink
import edu.byu.uapidsl.types.UAPILinks

class LinkGenerator<IdType: Any, ModelType: Any>(
    val model: IdentifiedResource<*, IdType, ModelType>
) {

    fun generateLinksFor(request: HttpRequest, id: IdType, resource: ModelType): UAPILinks {
        val prefix = this.model.name + '_'
        val result: UAPILinks = mutableMapOf(
            prefix + "self" to UAPILink("self", "", LinkMethod.GET)
        )

        return result
    }

}
