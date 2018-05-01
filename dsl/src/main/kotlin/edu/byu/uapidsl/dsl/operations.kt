package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.UApiMarker
import edu.byu.uapidsl.model.*
import either.Either
import either.Left
import either.Right
import kotlin.reflect.KClass


@UApiMarker
class OperationsInit<AuthContext, IdType, ResourceType> {

  @PublishedApi
  internal var createModel: CreateOperation<AuthContext, IdType, *>? by setOnce()

  inline fun <reified CreateModel : Any> create(init: CreateInit<AuthContext, IdType, CreateModel>.() -> Unit) {
    val createInit = CreateInit<AuthContext, IdType, CreateModel>(
      CreateModel::class
    )
    createInit.init()

    this.createModel = createInit.toModel()
  }

  @PublishedApi
  internal var updateModel: UpdateOperation<AuthContext, IdType, ResourceType, *>? by setOnce()

  inline fun <reified UpdateModel : Any> update(init: UpdateInit<AuthContext, IdType, ResourceType, UpdateModel>.() -> Unit) {
    val obj = UpdateInit<AuthContext, IdType, ResourceType, UpdateModel>(
      UpdateModel::class
    )
    obj.init()
    this.updateModel = obj.toModel()
  }

  inline fun <reified InputModel : Any> createOrUpdate(init: CreateOrUpdateInit<AuthContext, IdType, ResourceType, InputModel>.() -> Unit) {
    val obj = CreateOrUpdateInit<AuthContext, IdType, ResourceType, InputModel>(
      InputModel::class
    )
    obj.init()
    this.updateModel = obj.toModel()
  }

  var deleteModel: DeleteOperation<AuthContext, IdType, ResourceType>? by setOnce()

  inline fun delete(init: DeleteInit<AuthContext, IdType, ResourceType>.() -> Unit) {
    //TODO: Error if already set
    val obj = DeleteInit<AuthContext, IdType, ResourceType>()
    obj.init()
    this.deleteModel = obj.toModel()
  }

  var readModel: ReadOperation<AuthContext, IdType, ResourceType> by setOnce()

  inline fun read(init: ReadInit<AuthContext, IdType, ResourceType>.() -> Unit) {
    //TODO: Error if already set
    val obj = ReadInit<AuthContext, IdType, ResourceType>()
    obj.init()
    this.readModel = obj.toModel()
  }

  var listModel: ListOperation<AuthContext, IdType, ResourceType, *>? by setOnce()

  inline fun <reified FilterType: Any> listSimple(init: SimpleListInit<AuthContext, IdType, ResourceType, FilterType>.() -> Unit) {
    val obj = SimpleListInit<AuthContext, IdType, ResourceType, FilterType>(
      FilterType::class
    )
    obj.init()
    this.listModel = obj.toModel()
  }

  inline fun <reified FilterType: Any> listPaged(
    init: PagedCollectionInit<AuthContext, IdType, ResourceType, FilterType>.() -> Unit
  ) {
    val obj = PagedCollectionInit<AuthContext, IdType, ResourceType, FilterType>(
      FilterType::class
    )
    obj.init()
    this.listModel = obj.toModel()
  }

}

@UApiMarker
class ReadInit<AuthContext, IdType, ResourceModel> {

  lateinit var authorizer: ReadAuthorizer<AuthContext, IdType, ResourceModel>

  fun authorization(auth: ReadAuthorizer<AuthContext, IdType, ResourceModel>) {
    this.authorizer = auth
  }

  lateinit var handler: ReadHandler<AuthContext, IdType, ResourceModel>

  fun handle(handler: ReadHandler<AuthContext, IdType, ResourceModel>) {
    this.handler = handler
  }

  fun toModel(): ReadOperation<AuthContext, IdType, ResourceModel> {
    return ReadOperation(
      this.authorizer,
      this.handler
    )
  }

}

@UApiMarker
class SimpleListInit<AuthContext, IdType, ResourceModel, FilterType : Any>(
  private val filterType: KClass<FilterType>
) {

  private lateinit var handler: Either<
    ListHandler<AuthContext, FilterType, IdType>,
    ListHandler<AuthContext, FilterType, ResourceModel>
    >

  fun listIds(block: ListHandler<AuthContext, FilterType, IdType>) {
    handler = Left(block)
  }

  fun listObjects(block: ListHandler<AuthContext, FilterType, ResourceModel>) {
    handler = Right(block)
  }

  fun toModel(): SimpleListOperation<AuthContext, IdType, ResourceModel, FilterType> {
    return SimpleListOperation(
      filterType = Introspectable(filterType),
      handle = handler
    )
  }
}

@UApiMarker
class PagedCollectionInit<AuthContext, IdType, ResourceModel, FilterType: Any>(
  private val filterType: KClass<FilterType>
) {
  var defaultSize: Int = Int.MAX_VALUE
  var maxSize: Int = Int.MAX_VALUE

  fun listIds(block: PagedListHandler<AuthContext, FilterType, IdType>) {
    this.handler = Left(block)
  }

  fun listObjects(block: PagedListHandler<AuthContext, FilterType, ResourceModel>) {
    this.handler = Right(block)
  }

  private lateinit var handler: Either<
    PagedListHandler<AuthContext, FilterType, IdType>,
    PagedListHandler<AuthContext, FilterType, ResourceModel>
    >

  fun toModel(): PagedListOperation<AuthContext, IdType, ResourceModel, FilterType> {
    return PagedListOperation(
      filterType = Introspectable(filterType),
      handle = handler
    )
  }
}

typealias ListHandler<AuthContext, FilterType, ResultType> =
  ListContext<AuthContext, FilterType>.() -> Collection<ResultType>

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
class CreateInit<AuthContext, IdType, CreateModel : Any>(
  val input: KClass<CreateModel>
) {

  lateinit var authorizationHandler: CreateAuthorizer<AuthContext, CreateModel>

  fun authorization(auth: CreateAuthorizer<AuthContext, CreateModel>) {
    authorizationHandler = auth
  }

  lateinit var handler: CreateHandler<AuthContext, IdType, CreateModel>

  fun handle(handler: CreateHandler<AuthContext, IdType, CreateModel>) {
    this.handler = handler
  }

  fun toModel(): CreateOperation<AuthContext, IdType, CreateModel> = CreateOperation(
    input = Introspectable(input),
    authorization = this.authorizationHandler,
    handle = this.handler
  )
}

@UApiMarker
class UpdateInit<AuthContext, IdType, ResourceModel, InputModel : Any>(
  val input: KClass<InputModel>
) {

  lateinit var authorization: UpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel>
  fun authorization(auth: UpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel>) {
    this.authorization = auth
  }

  lateinit var handler: UpdateHandler<AuthContext, IdType, ResourceModel, InputModel>

  fun handle(handler: UpdateHandler<AuthContext, IdType, ResourceModel, InputModel>) {
    this.handler = handler
  }

  fun toModel(): SimpleUpdateOperation<AuthContext, IdType, ResourceModel, InputModel> {
    return SimpleUpdateOperation(
      input = Introspectable(this.input),
      authorization = this.authorization,
      handle = this.handler
    )
  }
}

@UApiMarker
class CreateOrUpdateInit<AuthContext, IdType, ResourceModel, InputModel : Any>(
  val input: KClass<InputModel>
) {

  lateinit var authorization: CreateOrUpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel>
  fun authorization(auth: CreateOrUpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel>) {
    this.authorization = auth
  }

  lateinit var handler: CreateOrUpdateHandler<AuthContext, IdType, ResourceModel, InputModel>

  fun handle(handler: CreateOrUpdateHandler<AuthContext, IdType, ResourceModel, InputModel>) {
    this.handler = handler
  }

  fun toModel(): CreateOrUpdateOperation<AuthContext, IdType, ResourceModel, InputModel> {
    return CreateOrUpdateOperation(
      input = Introspectable(this.input),
      authorization = this.authorization,
      handle = this.handler
    )
  }
}

@UApiMarker
class DeleteInit<AuthContext, IdType, ResourceModel> {

  lateinit var authorization: DeleteAuthorizer<AuthContext, IdType, ResourceModel>

  fun authorization(auth: DeleteAuthorizer<AuthContext, IdType, ResourceModel>) {
    this.authorization = auth
  }

  lateinit var handler: DeleteHandler<AuthContext, IdType, ResourceModel>

  fun handle(handler: DeleteHandler<AuthContext, IdType, ResourceModel>) {
    this.handler = handler
  }

  fun toModel(): DeleteOperation<AuthContext, IdType, ResourceModel> {
    return DeleteOperation(
      authorization = this.authorization,
      handle = this.handler
    )
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
