package edu.byu.uapidsl.model.resource

import edu.byu.uapidsl.dsl.CollectionWithTotal

typealias IdExtractor<IdType, ModelType> = (ModelType) -> IdType
typealias IsRestrictedFunc<AuthContext, IdType, DomainType> = ReadContext<AuthContext, IdType, DomainType>.() -> Boolean
typealias ListHandler<AuthContext, FilterType, ResultType> =
    ListContext<AuthContext, FilterType>.() -> Collection<ResultType>

typealias PagedListHandler<AuthContext, FilterType, ResultType> =
    PagedListContext<AuthContext, FilterType>.() -> CollectionWithTotal<ResultType>

typealias CreateHandler<AuthContext, IdType, CreateModel> =
    CreateContext<AuthContext, CreateModel>.() -> IdType

typealias UpdateHandler<AuthContext, IdType, ResourceModel, UpdateModel> =
    UpdateContext<AuthContext, IdType, ResourceModel, UpdateModel>.() -> Unit

typealias CreateOrUpdateHandler<AuthContext, IdType, ResourceModel, InputModel> =
    CreateOrUpdateContext<AuthContext, IdType, ResourceModel, InputModel>.() -> Unit

typealias DeleteHandler<AuthContext, IdType, ResourceModel> =
    DeleteContext<AuthContext, IdType, ResourceModel>.() -> Unit

typealias ReadHandler<AuthContext, IdType, ResourceModel> =
    ReadLoadContext<AuthContext, IdType>.() -> ResourceModel?

typealias ReadAuthorizer<AuthContext, IdType, ResourceModel> =
    ReadContext<AuthContext, IdType, ResourceModel>.() -> Boolean

typealias CreateAuthorizer<AuthContext, CreateModel> =
    CreateContext<AuthContext, CreateModel>.() -> Boolean

typealias CreateAllower<AuthContext, CreateModel> =
    CreateContext<AuthContext, CreateModel>.() -> Boolean

typealias CreateValidator<AuthContext, CreateModel> =
    CreateContext<AuthContext, CreateModel>.() -> Boolean

typealias UpdateAuthorizer<AuthContext, IdType, ResourceModel, UpdateModel> =
    UpdateContext<AuthContext, IdType, ResourceModel, UpdateModel>.() -> Boolean

typealias UpdateAllower<AuthContext, IdType, ResourceModel, UpdateModel> =
    UpdateContext<AuthContext, IdType, ResourceModel, UpdateModel>.() -> Boolean

typealias CreateOrUpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel> =
    CreateOrUpdateContext<AuthContext, IdType, ResourceModel, InputModel>.() -> Boolean

typealias CreateOrUpdateAllower<AuthContext, IdType, ResourceModel, InputModel> =
    CreateOrUpdateContext<AuthContext, IdType, ResourceModel, InputModel>.() -> Boolean

typealias DeleteAuthorizer<AuthContext, IdType, ResourceModel> =
    DeleteContext<AuthContext, IdType, ResourceModel>.() -> Boolean

