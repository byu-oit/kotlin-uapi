package edu.byu.uapidsl.dsl.subresource.single

import edu.byu.uapidsl.DslPart
import edu.byu.uapidsl.ModelingContext


class SingleSubResourceDSL<AuthContext, ParentId, ParentModel, SingleSubResourceModel>(
    val name: String
) : DslPart<Nothing>() {
    override fun toModel(context: ModelingContext): Nothing {
        TODO("not implemented")
    }

    inline fun operations(init: SingleSubOperationsDSL<AuthContext, ParentId, ParentModel, SingleSubResourceModel>.() -> Unit) {

    }

    inline fun model(init: SingleSubModelDSL<AuthContext, ParentId, ParentModel, SingleSubResourceModel>.() -> Unit) {

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
