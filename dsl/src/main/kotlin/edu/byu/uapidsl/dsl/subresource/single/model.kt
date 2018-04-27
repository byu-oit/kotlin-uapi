package edu.byu.uapidsl.dsl.subresource.single

import edu.byu.uapidsl.UApiMarker


@UApiMarker
class SingleSubModelInit<AuthContext, ParentId, ParentModel, SingleSubModel> {

    fun customizeFields(handler: SingleSubCustomizeFieldsHandler<AuthContext, ParentId, ParentModel, SingleSubModel>) {

    }

    inline fun <RelatedId, reified RelatedModel> relation(
            name: String,
            init: SingleSubRelationInit<AuthContext, ParentId, ParentModel, SingleSubModel, RelatedId, RelatedModel>.() -> Unit
            ) {
    }

    inline fun externalRelation(
            name: String,
            init: SingleSubExternalRelationInit<AuthContext, ParentId, ParentModel, SingleSubModel>.() -> Unit
    ) {

    }

}

@UApiMarker
class SingleSubRelationInit<AuthContext, ParentId, ParentModel, SingleSubModel, RelatedId, RelatedModel> {
    fun authorization(authorizer: SingleSubRelationAuthorizer<AuthContext, ParentId, ParentModel, SingleSubModel, RelatedId, RelatedModel>) {

    }

    fun handle(handler: SingleSubRelationHandler<AuthContext, ParentId, ParentModel, SingleSubModel, RelatedId>) {

    }
}

@UApiMarker
class SingleSubExternalRelationInit<AuthContext, ParentId, ParentModel, SingleSubModel> {
    fun authorization(authorizer: SingleSubExternalRelationAuthorizer<AuthContext, ParentId, ParentModel, SingleSubModel>) {

    }

    fun handle(handler: SingleSubExternalRelationHandler<AuthContext, ParentId, ParentModel, SingleSubModel>) {

    }
}

typealias SingleSubExternalRelationAuthorizer<AuthContext, ParentId, ParentModel, SingleSubModel> =
        (SingleSubExternalRelationContext<AuthContext, ParentId, ParentModel, SingleSubModel>) -> Boolean

typealias SingleSubExternalRelationHandler<AuthContext, ParentId, ParentModel, SingleSubModel> =
        (SingleSubExternalRelationContext<AuthContext, ParentId, ParentModel, SingleSubModel>) -> String?

interface SingleSubExternalRelationContext<AuthContext, ParentId, ParentModel, SingleSubModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val SingleSubresource: SingleSubModel
}

typealias SingleSubRelationHandler<AuthContext, ParentId, ParentModel, SingleSubModel, RelatedId> =
        (context: SingleSubRelationLoadingContext<AuthContext, ParentId, ParentModel, SingleSubModel>) -> RelatedId?

interface SingleSubRelationLoadingContext<AuthContext, ParentId, ParentModel, SingleSubModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val SingleSubresource: SingleSubModel
}

interface SingleSubRelationAuthorizationContext<AuthContext, ParentId, ParentModel, SingleSubModel, RelatedId, RelatedType> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val SingleSubresource: SingleSubModel
    val relatedId: RelatedId
    val relatedType: RelatedType
}

typealias SingleSubRelationAuthorizer<AuthContext, ParentId, ParentModel, SingleSubModel, RelatedId, RelatedModel> =
  (context: SingleSubRelationAuthorizationContext<AuthContext, ParentId, ParentModel, SingleSubModel, RelatedId, RelatedModel>) -> Boolean

typealias SingleSubCustomizeFieldsHandler<AuthContext, ParentId, ParentModel, SingleSubModel> =
        (SingleSubCustomizeFieldsContext<AuthContext, ParentId, ParentModel, SingleSubModel>) -> SingleSubModel

interface SingleSubCustomizeFieldsContext<AuthContext, ParentId, ParentModel, SingleSubModel> {
    val authContext: AuthContext
    val parentId: ParentId
    val parent: ParentModel
    val SingleSubresource: SingleSubModel
}

