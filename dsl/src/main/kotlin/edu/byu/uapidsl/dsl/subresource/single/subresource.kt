package edu.byu.uapidsl.dsl.subresource.single

import edu.byu.uapidsl.UApiMarker


@UApiMarker
class SingleSubResourceInit<AuthContext, ParentId, ParentModel, SingleSubResourceModel>(
        val name: String
) {

    inline fun operations(init: SingleSubOperationsInit<AuthContext, ParentId, ParentModel, SingleSubResourceModel>.() -> Unit) {

    }

    inline fun model(init: SingleSubModelInit<AuthContext, ParentId, ParentModel, SingleSubResourceModel>.() -> Unit) {

    }

    fun authorization(init: SingleSubAuthorizationHandler<AuthContext, ParentId, ParentModel>) {

    }

}

typealias SingleSubAuthorizationHandler<AuthContext, ParentId, ParentModel> =
        SingleSubAuthorizationContext<AuthContext, ParentId, ParentModel>.() -> Boolean

interface SingleSubAuthorizationContext<AuthContext, ParentId, ParentModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parentModel: ParentModel
}
