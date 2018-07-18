package edu.byu.uapidsl.model.resource

import edu.byu.uapidsl.dsl.PagingParams

interface AuthorizedContext<AuthContext> {
    val authContext: AuthContext
}

interface ListContext<AuthContext, FilterType> : AuthorizedContext<AuthContext> {
    val filters: FilterType
}

interface PagedListContext<AuthContext, FilterType> : ListContext<AuthContext, FilterType> {
    val paging: PagingParams
}

interface IdentifiedContext<AuthContext, IdType> : AuthorizedContext<AuthContext> {
    val id: IdType
}

interface IdentifiedResourceContext<AuthContext, IdType, ModelType> : IdentifiedContext<AuthContext, IdType> {
    val resource: ModelType
}

interface CreateContext<AuthContext, CreateModel> : AuthorizedContext<AuthContext>, InputContext<CreateModel>
interface ReadLoadContext<AuthContext, IdType> : IdentifiedContext<AuthContext, IdType>
interface ReadContext<AuthContext, IdType, ModelType> : IdentifiedResourceContext<AuthContext, IdType, ModelType>
interface InputContext<InputModel> {
    val input: InputModel
}

interface UpdateContext<AuthContext, IdType, ModelType, UpdateModel> : IdentifiedResourceContext<AuthContext, IdType, ModelType>, InputContext<UpdateModel>
interface CreateOrUpdateContext<AuthContext, IdType, ModelType, UpdateModel> : IdentifiedContext<AuthContext, IdType>, InputContext<UpdateModel> {
    val resource: ModelType?
}

interface DeleteContext<AuthContext, IdType, ModelType> : IdentifiedResourceContext<AuthContext, IdType, ModelType>
