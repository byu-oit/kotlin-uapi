package edu.byu.uapidsl.dsl.subresource.list

import edu.byu.uapidsl.UApiMarker


@UApiMarker
class SubModelInit<AuthContext, ParentId, ParentModel, SubId, SubModel> {

    fun customizeFields(handler: SubCustomizeFieldsHandler<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

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

@UApiMarker
class SubRelationInit<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId, RelatedModel> {
    fun authorization(authorizer: SubRelationAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId, RelatedModel>) {

    }

    fun handle(handler: SubRelationHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId>) {

    }
}

@UApiMarker
class SubExternalRelationInit<AuthContext, ParentId, ParentModel, SubId, SubModel> {
    fun authorization(authorizer: SubExternalRelationAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }

    fun handle(handler: SubExternalRelationHandler<AuthContext, ParentId, ParentModel, SubId, SubModel>) {

    }
}

typealias SubExternalRelationAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel> =
        (SubExternalRelationContext<AuthContext, ParentId, ParentModel, SubId, SubModel>) -> Boolean

typealias SubExternalRelationHandler<AuthContext, ParentId, ParentModel, SubId, SubModel> =
        (SubExternalRelationContext<AuthContext, ParentId, ParentModel, SubId, SubModel>) -> String?

interface SubExternalRelationContext<AuthContext, ParentId, ParentModel, SubId, SubModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val id: SubId
    val subresource: SubModel
}

typealias SubRelationHandler<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId> =
        (context: SubRelationLoadingContext<AuthContext, ParentId, ParentModel, SubId, SubModel>) -> RelatedId?

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

typealias SubRelationAuthorizer<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId, RelatedModel> =
  (context: SubRelationAuthorizationContext<AuthContext, ParentId, ParentModel, SubId, SubModel, RelatedId, RelatedModel>) -> Boolean

typealias SubCustomizeFieldsHandler<AuthContext, ParentId, ParentModel, SubId, SubModel> =
        (SubCustomizeFieldsContext<AuthContext, ParentId, ParentModel, SubId, SubModel>) -> SubModel

interface SubCustomizeFieldsContext<AuthContext, ParentId, ParentModel, SubId, SubModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val id: SubId
    val subresource: SubModel
}

