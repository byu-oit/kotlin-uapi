package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.UApiMarker


@UApiMarker
class ResourceInit<AuthContext, IdType, ResourceModel>(
        val name: String
) {

    inline fun operations(init: OperationsInit<AuthContext, IdType, ResourceModel>.() -> Unit) {

    }

    inline fun model(init: ModelInit<AuthContext, IdType, ResourceModel>.() -> Unit) {

    }

}
