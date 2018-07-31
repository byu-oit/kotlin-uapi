package edu.byu.uapidsl.dsl

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

//class OutputInit<AuthContext, IdType, IdentifiedResource : Any, UAPIType: Any>(
//    validation: Validating,
//    private val type: Introspectable<UAPIType>
//) : DslPart(validation) {
//
//    var example: UAPIType by setOnce()
//
//    var transformModel: TransformModelHandler<AuthContext, IdType, IdentifiedResource, UAPIType> by setOnce()
//
//    fun transform(block: TransformModelHandler<AuthContext, IdType, IdentifiedResource, UAPIType>) {
//        this.transformModel = block
//    }
//
////  inline fun <RelatedId, reified RelatedModel> relation(
////    name: String,
////    init: RelationInit<AuthContext, IdType, IdentifiedResource, RelatedId, RelatedModel>.() -> Unit
////  ) {
////  }
////
////  inline fun externalRelation(
////    name: String,
////    init: ExternalRelationInit<AuthContext, IdType, IdentifiedResource>.() -> Unit
////  ) {
////
////  }
//
////    fun toModel(): OutputModel<AuthContext, IdType, IdentifiedResource, UAPIType> {
////        return OutputModel(
////            type = this.type,
////            example = this.example,
////            handle = this.transformModel
////        )
////    }
//
//}

//class RelationInit<AuthContext, IdType, IdentifiedResource, RelatedId, RelatedModel>(
//    validation: Validating
//) : DslPart(validation) {
//    fun authorization(authorizer: RelationAuthorizer<AuthContext, IdType, IdentifiedResource, RelatedId, RelatedModel>) {
//
//    }
//
//    fun handle(handler: RelationHandler<AuthContext, IdType, IdentifiedResource, RelatedId>) {
//
//    }
//}

//class ExternalRelationInit<AuthContext, IdType, IdentifiedResource>(
//    validation: Validating
//) : DslPart(validation) {
//    fun authorization(authorizer: ExternalRelationAuthorizer<AuthContext, IdType, IdentifiedResource>) {
//
//    }
//
//    fun handle(handler: ExternalRelationHandler<AuthContext, IdType, IdentifiedResource>) {
//
//    }
//}

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

