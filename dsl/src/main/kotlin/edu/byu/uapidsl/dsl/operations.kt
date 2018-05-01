package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.UApiMarker


@UApiMarker
class OperationsInit<AuthContext, IdType, ResourceModel> {
  inline fun <reified CreateModel> create(init: CreateInit<AuthContext, IdType, CreateModel>.() -> Unit) {

  }

  inline fun <reified UpdateModel> update(init: UpdateInit<AuthContext, IdType, ResourceModel, UpdateModel>.() -> Unit) {

  }

  inline fun <reified InputModel> createOrUpdate(init: CreateOrUpdateInit<AuthContext, IdType, ResourceModel, InputModel>.() -> Unit) {

  }

  inline fun delete(init: DeleteInit<AuthContext, IdType, ResourceModel>.() -> Unit) {

  }

  inline fun read(init: ReadInit<AuthContext, IdType, ResourceModel>.() -> Unit) {

  }

  inline fun <reified FilterType> listSimple(handler: ListHandler<AuthContext, IdType, FilterType>) {

  }

  inline fun <reified FilterType> listPaged(
    init: PagedCollectionInit<AuthContext, IdType, ResourceModel, FilterType>.() -> Unit
  ) {

  }

}

@UApiMarker
class ReadInit<AuthContext, IdType, ResourceModel> {
  fun authorization(auth: ReadAuthorizer<AuthContext, IdType, ResourceModel>) {

  }

  fun handle(handler: ReadLoadContext<AuthContext, IdType>.() -> ResourceModel?) {
    TODO()
  }

}

@UApiMarker
class PagedCollectionInit<AuthContext, IdType, ResourceModel, FilterType> {
  var defaultSize: Int = Int.MAX_VALUE
  var maxSize: Int = Int.MAX_VALUE

  fun listIds(block: PagedListHandler<AuthContext, FilterType, IdType>) {

  }

  fun listObjects(handler: PagedListHandler<AuthContext, FilterType, ResourceModel>) {

  }
}

typealias ListHandler<AuthContext, IdType, FilterType> =
  ListContext<AuthContext, FilterType>.() -> Collection<IdType>

interface ListContext<AuthContext, FilterType> {
  val authContext: AuthContext
  val filters: FilterType
}

typealias PagedListHandler<AuthContext, FilterType, ResultType> =
  PagedListContext<AuthContext, FilterType>.() -> CollectionWithTotal<ResultType>

interface PagedListContext<AuthContext, FilterType> {
  val authContext: AuthContext
  val filters: FilterType
  val paging: PagingParams
}


@UApiMarker
class CreateInit<AuthContext, IdType, CreateModel> {
  fun authorization(auth: CreateAuthorizer<AuthContext, CreateModel>) {

  }

  fun handle(handler: CreateHandler<AuthContext, IdType, CreateModel>) {

  }
}

@UApiMarker
class UpdateInit<AuthContext, IdType, ResourceModel, UpdateModel> {
  fun authorization(auth: UpdateAuthorizer<AuthContext, IdType, ResourceModel, UpdateModel>) {

  }

  fun handle(handler: UpdateHandler<AuthContext, IdType, ResourceModel, UpdateModel>) {

  }
}

@UApiMarker
class CreateOrUpdateInit<AuthContext, IdType, ResourceModel, UpdateModel> {
  fun authorization(auth: CreateOrUpdateAuthorizer<AuthContext, IdType, ResourceModel, UpdateModel>) {

  }

  fun handle(handler: CreateOrUpdateHandler<AuthContext, IdType, ResourceModel, UpdateModel>) {

  }
}

@UApiMarker
class DeleteInit<AuthContext, IdType, ResourceModel> {
  fun authorization(auth: DeleteAuthorizer<AuthContext, IdType, ResourceModel>) {

  }

  fun handle(handler: DeleteHandler<AuthContext, IdType, ResourceModel>) {

  }
}



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

typealias UpdateAuthorizer<AuthContext, IdType, ResourceModel, UpdateModel> =
  UpdateContext<AuthContext, IdType, ResourceModel, UpdateModel>.() -> Boolean

typealias CreateOrUpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel> =
  CreateOrUpdateContext<AuthContext, IdType, ResourceModel, InputModel>.() -> Boolean

typealias DeleteAuthorizer<AuthContext, IdType, ResourceModel> =
  DeleteContext<AuthContext, IdType, ResourceModel>.() -> Boolean

interface CreateContext<AuthContext, CreateModel> {
  val authContext: AuthContext
  val input: CreateModel
}

@UApiMarker
interface ReadLoadContext<AuthContext, IdType> {
  val authContext: AuthContext
  val id: IdType
}

interface ReadContext<AuthContext, IdType, ResourceModel> {
  val authContext: AuthContext
  val id: IdType
  val resource: ResourceModel
}

interface UpdateContext<AuthContext, IdType, ResourceModel, UpdateModel> {
  val authContext: AuthContext
  val id: IdType
  val input: UpdateModel
  val resource: ResourceModel
}

interface CreateOrUpdateContext<AuthContext, IdType, ResourceModel, UpdateModel> {
  val authContext: AuthContext
  val id: IdType
  val input: UpdateModel
  val resource: ResourceModel?
}

interface DeleteContext<AuthContext, IdType, ResourceModel> {
  val authContext: AuthContext
  val id: IdType
  val resource: ResourceModel
}
