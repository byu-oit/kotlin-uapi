package edu.byu.uapidsl.subresource.list

import edu.byu.uapidsl.UApiMarker


@UApiMarker
class SubResourceInit<AuthContext, ParentId, ParentModel, SubResourceId, SubResourceModel>(
        val name: String
) {

    inline fun operations(init: SubOperationsInit<AuthContext, ParentId, ParentModel, SubResourceId, SubResourceModel>.() -> Unit) {

    }

    inline fun model(init: SubModelInit<AuthContext, ParentId, ParentModel, SubResourceId, SubResourceModel>.() -> Unit) {

    }

    fun authorization(init: SubAuthorizationHandler<AuthContext, ParentId, ParentModel>) {

    }

}

typealias SubAuthorizationHandler<AuthContext, ParentId, ParentModel> =
        (SubAuthorizationContext<AuthContext, ParentId, ParentModel>) -> Boolean

interface SubAuthorizationContext<AuthContext, ParentId, ParentModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parentModel: ParentModel
}
