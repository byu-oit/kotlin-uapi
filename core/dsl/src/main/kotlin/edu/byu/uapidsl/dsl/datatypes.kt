package edu.byu.uapidsl.dsl

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

//data class ApiDefinition<AuthContext>(
//        val resources: Map<KClass<*>, ApiResource<AuthContext, *, *>>
//)
//
//data class ApiResource<AuthContext, IdType, IdentifiedResource>(
//  val name: String,
//  val authorized: ResourceAuthorization<AuthContext, IdType, IdentifiedResource>,
//  val loader: ResourceLoader<IdType, IdentifiedResource>,
//  val collection: CollectionDefinition<IdType, *>,
//  val fieldCustomizer: FieldCustomizer<AuthContext, IdentifiedResource>
//)
//
//data class ResourceAuthorization<AuthContext, IdType, IdentifiedResource>(
//        val string: String
//)
//
//
//typealias ResourceLoader<IdType, IdentifiedResource> = (IdType) -> IdentifiedResource
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
//typealias FieldCustomizer<AuthContext, IdentifiedResource> = (AuthContext, IdentifiedResource, String) -> UAPIField<*>
//
//data class UAPIField<Type>(
//        val value: Type?
//)

data class CollectionWithTotal<IdType>(
    val totalItems: Int,
    private val values: Collection<IdType>
): Collection<IdType> by values

data class PagingParams(
    val pageStart: Int,
    val pageSize: Int
)
