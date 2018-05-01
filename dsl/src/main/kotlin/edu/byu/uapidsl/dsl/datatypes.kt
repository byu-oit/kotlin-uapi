package edu.byu.uapidsl.dsl

//data class ApiDefinition<AuthContext>(
//        val resources: Map<KClass<*>, ApiResource<AuthContext, *, *>>
//)
//
//data class ApiResource<AuthContext, IdType, ResourceModel>(
//  val name: String,
//  val authorization: ResourceAuthorization<AuthContext, IdType, ResourceModel>,
//  val loader: ResourceLoader<IdType, ResourceModel>,
//  val collection: CollectionDefinition<IdType, *>,
//  val fieldCustomizer: FieldCustomizer<AuthContext, ResourceModel>
//)
//
//data class ResourceAuthorization<AuthContext, IdType, ResourceModel>(
//        val string: String
//)
//
//
//typealias ResourceLoader<IdType, ResourceModel> = (IdType) -> ResourceModel
//
//interface CollectionDefinition<IdType, FilterType>
//
//data class SimpleCollectionDefinition<IdType, FilterType>(
//        val loader: SimpleCollectionLoader<IdType, FilterType>
//) : CollectionDefinition<IdType, FilterType>
//
//data class PagedCollectionDefinition<IdType, FilterType>(
//        val loader: PagedCollectionLoader<IdType, FilterType>
//) : CollectionDefinition<IdType, FilterType>
//
//typealias SimpleCollectionLoader<IdType, FilterType> = (FilterType) -> Collection<IdType>
//typealias PagedCollectionLoader<IdType, FilterType> = (FilterType, PagingParams) -> CollectionWithTotal<IdType>
//
//typealias FieldCustomizer<AuthContext, ResourceModel> = (AuthContext, ResourceModel, String) -> UAPIField<*>
//
//data class UAPIField<Type>(
//        val value: Type?
//)

data class CollectionWithTotal<IdType>(
        val totalItems: Int,
        val ids: Collection<IdType>
)

data class PagingParams(
        val pageStart: Int
)
