package edu.byu.uapidsl

import edu.byu.uapidsl.subresource.list.SubResourceInit
import edu.byu.uapidsl.subresource.single.SingleSubResourceInit


@UApiMarker
class ModelInit<AuthContext, IdType, ResourceModel> {

    fun customizeFields(handler: CustomizeFieldsHandler<AuthContext, IdType, ResourceModel>) {

    }

    inline fun <RelatedId, reified RelatedModel> relation(
            name: String,
            init: RelationInit<AuthContext, IdType, ResourceModel, RelatedId, RelatedModel>.() -> Unit
            ) {
    }

    inline fun externalRelation(
            name: String,
            init: ExternalRelationInit<AuthContext, IdType, ResourceModel>.() -> Unit
    ) {

    }

    inline fun <SubResourceId, reified SubResourceModel> collectionSubresource(
            name: String,
            init: SubResourceInit<AuthContext, IdType, ResourceModel, SubResourceId, SubResourceModel>.() -> Unit
    ) {

    }

  inline fun <reified SubResourceModel> singleSubresource(
    name: String,
    init: SingleSubResourceInit<AuthContext, IdType, ResourceModel, SubResourceModel>.() -> Unit
  ) {

  }

}

@UApiMarker
class RelationInit<AuthContext, IdType, ResourceModel, RelatedId, RelatedModel> {
    fun authorization(authorizer: RelationAuthorizer<AuthContext, IdType, ResourceModel, RelatedId, RelatedModel>) {

    }

    fun handle(handler: RelationHandler<AuthContext, IdType, ResourceModel, RelatedId>) {

    }
}

@UApiMarker
class ExternalRelationInit<AuthContext, IdType, ResourceModel> {
    fun authorization(authorizer: ExternalRelationAuthorizer<AuthContext, IdType, ResourceModel>) {

    }

    fun handle(handler: ExternalRelationHandler<AuthContext, IdType, ResourceModel>) {

    }
}

typealias ExternalRelationAuthorizer<AuthContext, IdType, ResourceModel> =
        (ExternalRelationContext<AuthContext, IdType, ResourceModel>) -> Boolean

typealias ExternalRelationHandler<AuthContext, IdType, ResourceModel> =
        (ExternalRelationContext<AuthContext, IdType, ResourceModel>) -> String?

interface ExternalRelationContext<AuthContext, IdType, ResourceModel> {
    val authContext: AuthContext
    val idType: IdType
    val resource: ResourceModel
}

typealias RelationHandler<AuthContext, IdType, ResourceModel, RelatedId> =
        (context: RelationLoadingContext<AuthContext, IdType, ResourceModel>) -> RelatedId?

interface RelationLoadingContext<AuthContext, IdType, ResourceModel> {
    val authContext: AuthContext
    val id: IdType
    val resource: ResourceModel
}

interface RelationAuthorizationContext<AuthContext, IdType, ResourceModel, RelatedId, RelatedType> {
    val authContext: AuthContext
    val id: IdType
    val resource: ResourceModel
    val relatedId: RelatedId
    val relatedType: RelatedType
}

typealias RelationAuthorizer<AuthContext, IdType, ResourceModel, RelatedId, RelatedModel> =
  (context: RelationAuthorizationContext<AuthContext, IdType, ResourceModel, RelatedId, RelatedModel>) -> Boolean

typealias CustomizeFieldsHandler<AuthContext, IdType, ResourceModel> =
        (CustomizeFieldsContext<AuthContext, IdType, ResourceModel>) -> ResourceModel

interface CustomizeFieldsContext<AuthContext, IdType, ResourceModel> {
    val authContext: AuthContext
    val id: IdType
    val resource: ResourceModel
}

