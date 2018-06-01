package edu.byu.uapidsl.dsl.subresource.list

import edu.byu.uapidsl.DSLInit
import edu.byu.uapidsl.ModelingContext
import edu.byu.uapidsl.ValidationContext

class SubResourceInit<AuthContext, ParentId, ParentModel, SubResourceId, SubResourceModel>(
    validation: ValidationContext,
    val name: String
) : DSLInit<SubResourceModel>(validation) {

    inline fun operations(init: SubOperationsInit<AuthContext, ParentId, ParentModel, SubResourceId, SubResourceModel>.() -> Unit) {

    }

//    inline fun model(init: SubModelInit<AuthContext, ParentId, ParentModel, SubResourceId, SubResourceModel>.() -> Unit) {
//    }

    fun authorization(init: SubAuthorizationHandler<AuthContext, ParentId, ParentModel>) {

    }

    override fun toModel(context: ModelingContext): SubResourceModel {
        TODO("not implemented")
    }
}

class SubResourceModel

typealias SubAuthorizationHandler<AuthContext, ParentId, ParentModel> =
    SubAuthorizationContext<AuthContext, ParentId, ParentModel>.() -> Boolean

interface SubAuthorizationContext<AuthContext, ParentId, ParentModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parentModel: ParentModel
}
