package edu.byu.uapidsl.dsl.subresource.list

import edu.byu.uapidsl.DSLInit
import edu.byu.uapidsl.ValidationContext


class SubModelInit<AuthContext, ParentId, ParentModel, SubId, SubModel>(
    validation: ValidationContext
) : DSLInit(validation) {

    fun <UAPIType> transform(handler: SubTransformer<AuthContext, ParentId, ParentModel, SubId, SubModel, UAPIType>) {

    }

    inline fun <RelatedId, reified RelatedModel> relation(
        name: String,
        init: SubRelationInit<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId, RelatedModel>.() -> Unit
    ) {
    }

    inline fun externalRelation(
        name: String,
        init: SubExternalRelationInit<AuthContext, ParentId, ParentModel, SubId, SubModel>.() -> Unit
    ) {

    }

}

class SubRelationInit<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId, RelatedModel>(
    validation: ValidationContext
) : DSLInit(validation) {
    fun authorization(authorizer: SubRelationAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId, RelatedModel>) {

    }

    fun handle(handler: SubRelationHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId>) {

    }
}

class SubExternalRelationInit<AuthContext, ParentId, ParentModel, SubId, SubModel>(
    validation: ValidationContext
) : DSLInit(validation) {
    fun authorization(authorizer: SubExternalRelationAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }

    fun handle(handler: SubExternalRelationHandler<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }
}

interface SubExternalRelationContext<AuthContext, ParentId, ParentModel, SubId, SubModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val id: SubId
    val subresource: SubModel
}

interface SubRelationLoadingContext<AuthContext, ParentId, ParentModel, SubId, SubModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val id: SubId
    val subresource: SubModel
}

interface SubRelationAuthorizationContext<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId, RelatedType> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val id: SubId
    val subresource: SubModel
    val relatedId: RelatedId
    val relatedType: RelatedType
}

interface SubCustomizeFieldsContext<AuthContext, ParentId, ParentModel, SubId, SubModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val id: SubId
    val subresource: SubModel
}

typealias SubExternalRelationAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel> =
    SubExternalRelationContext<AuthContext, ParentId, ParentModel, SubId, SubModel>.() -> Boolean

typealias SubExternalRelationHandler<AuthContext, ParentId, ParentModel, SubId, SubModel> =
    SubExternalRelationContext<AuthContext, ParentId, ParentModel, SubId, SubModel>.() -> String?

typealias SubRelationHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId> =
    SubRelationLoadingContext<AuthContext, ParentId, ParentModel, SubId, SubModel>.() -> RelatedId?

typealias SubRelationAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId, RelatedModel> =
    SubRelationAuthorizationContext<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId, RelatedModel>.() -> Boolean

typealias SubTransformer<AuthContext, ParentId, ParentModel, SubId, SubModel, UAPIType> =
    SubCustomizeFieldsContext<AuthContext, ParentId, ParentModel, SubId, SubModel>.() -> UAPIType

