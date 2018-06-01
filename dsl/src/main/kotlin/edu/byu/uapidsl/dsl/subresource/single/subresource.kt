package edu.byu.uapidsl.dsl.subresource.single

import edu.byu.uapidsl.DSLInit
import edu.byu.uapidsl.ModelingContext
import edu.byu.uapidsl.ValidationContext


class SingleSubResourceInit<AuthContext, ParentId, ParentModel, SingleSubResourceModel>(
    validation: ValidationContext,
    val name: String
) : DSLInit<Nothing>(validation) {
    override fun toModel(context: ModelingContext): Nothing {
        TODO("not implemented")
    }

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
