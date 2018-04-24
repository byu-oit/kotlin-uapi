package edu.byu.kotlin.uapidsl.subresource


class SubResourceInit<AuthContext, ParentId, ParentModel, SubResourceId, SubResourceModel>(
        val name: String
) {

    inline fun operations(init: SubOperationsInit<AuthContext, ParentId, ParentModel, SubResourceId, SubResourceModel>.() -> Unit) {

    }

//    inline fun model(init: SubModelInit<AuthContext, ParentId, ParentModel, SubResourceId, SubResourceModel>.() -> Unit) {
//
//    }

}
