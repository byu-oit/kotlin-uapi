package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.DSLInit
import edu.byu.uapidsl.ValidationContext
import edu.byu.uapidsl.dsl.subresource.list.SubResourceInit
import edu.byu.uapidsl.dsl.subresource.single.SingleSubResourceInit
import edu.byu.uapidsl.model.Introspectable
import edu.byu.uapidsl.model.TransformModel
import edu.byu.uapidsl.types.ApiType
import edu.byu.uapidsl.types.UAPIField
import java.net.URI

fun <Type> uapiProp(
  value: Type,
  apiType: ApiType = ApiType.MODIFIABLE,
  description: String? = null,
  longDescription: String? = null,
  displayLabel: String? = null
) = UAPIField.prop(
  value = value,
  apiType = apiType,
  description = description,
  longDescription = longDescription,
  displayLabel = displayLabel
)

fun <Type> uapiDomainProp(
  value: Type,
  apiType: ApiType = ApiType.MODIFIABLE,
  domain: URI,
  description: String? = null,
  longDescription: String? = null,
  displayLabel: String? = null
) = UAPIField.domainProp(
  value = value,
  apiType = apiType,
  description = description,
  longDescription = longDescription,
  displayLabel = displayLabel,
  domain = domain
)

fun <Type> uapiKey(
  value: Type,
  apiType: ApiType = ApiType.MODIFIABLE,
  description: String? = null,
  longDescription: String? = null,
  displayLabel: String? = null
) = UAPIField.key(
  value = value,
  apiType = apiType,
  description = description,
  longDescription = longDescription,
  displayLabel = displayLabel
)

class ModelInit<AuthContext, IdType, ResourceModel: Any>(
  validation: ValidationContext
) : DSLInit(validation) {

  var example: ResourceModel by setOnce()

  var transformModel: TransformModel<AuthContext, IdType, ResourceModel, *> by setOnce()

  inline fun <reified UAPIType: Any> transform(noinline block: TransformModelHandler<AuthContext, IdType, ResourceModel, UAPIType>) {
    this.transformModel = TransformModel(
      type = Introspectable(UAPIType::class),
      handle = block
    )
  }

//  inline fun <RelatedId, reified RelatedModel> relation(
//    name: String,
//    init: RelationInit<AuthContext, IdType, ResourceModel, RelatedId, RelatedModel>.() -> Unit
//  ) {
//  }
//
//  inline fun externalRelation(
//    name: String,
//    init: ExternalRelationInit<AuthContext, IdType, ResourceModel>.() -> Unit
//  ) {
//
//  }

  inline fun <reified SubResourceId, reified SubResourceModel> collectionSubresource(
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

class RelationInit<AuthContext, IdType, ResourceModel, RelatedId, RelatedModel>(
  validation: ValidationContext
): DSLInit(validation) {
  fun authorization(authorizer: RelationAuthorizer<AuthContext, IdType, ResourceModel, RelatedId, RelatedModel>) {

  }

  fun handle(handler: RelationHandler<AuthContext, IdType, ResourceModel, RelatedId>) {

  }
}

class ExternalRelationInit<AuthContext, IdType, ResourceModel>(
  validation: ValidationContext
): DSLInit(validation) {
  fun authorization(authorizer: ExternalRelationAuthorizer<AuthContext, IdType, ResourceModel>) {

  }

  fun handle(handler: ExternalRelationHandler<AuthContext, IdType, ResourceModel>) {

  }
}

typealias ExternalRelationAuthorizer<AuthContext, IdType, ResourceModel> =
  ExternalRelationContext<AuthContext, IdType, ResourceModel>.() -> Boolean

typealias ExternalRelationHandler<AuthContext, IdType, ResourceModel> =
  ExternalRelationContext<AuthContext, IdType, ResourceModel>.() -> String?

interface ExternalRelationContext<AuthContext, IdType, ResourceModel> {
  val authContext: AuthContext
  val idType: IdType
  val resource: ResourceModel
}

typealias RelationHandler<AuthContext, IdType, ResourceModel, RelatedId> =
  RelationLoadingContext<AuthContext, IdType, ResourceModel>.() -> RelatedId?

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
  RelationAuthorizationContext<AuthContext, IdType, ResourceModel, RelatedId, RelatedModel>.() -> Boolean

typealias TransformModelHandler<AuthContext, IdType, ResourceModel, UAPIType> =
        TransformModelContext<AuthContext, IdType, ResourceModel>.() -> UAPIType

interface TransformModelContext<AuthContext, IdType, ResourceModel> {
  val authContext: AuthContext
  val id: IdType
  val resource: ResourceModel
}

