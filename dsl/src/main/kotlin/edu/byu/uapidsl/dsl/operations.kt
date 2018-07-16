package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.DSLInit
import edu.byu.uapidsl.ModelingContext
import edu.byu.uapidsl.model.resource.*
import edu.byu.uapidsl.model.resource.ops.PagedListOperation
import edu.byu.uapidsl.model.resource.ops.SimpleListOperation
import either.Either
import either.Left
import either.Right
import either.fold
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


class OperationsInit<AuthContext, IdType, ResourceType> : DSLInit<OperationModel<AuthContext, IdType, ResourceType>>() {

    @PublishedApi
    internal var createInit: CreateInit<AuthContext, IdType, *>? by setOnce()

    inline fun <reified CreateModel : Any> create(init: CreateInit<AuthContext, IdType, CreateModel>.() -> Unit) {
        val createInit = CreateInit<AuthContext, IdType, CreateModel>(
            CreateModel::class
        )
        createInit.init()
        this.createInit = createInit
    }

    @PublishedApi
    internal var updateInit: Either<UpdateInit<AuthContext, IdType, ResourceType, *>, CreateOrUpdateInit<AuthContext, IdType, ResourceType, *>>? by setOnce()

    inline fun <reified UpdateModel : Any> update(init: UpdateInit<AuthContext, IdType, ResourceType, UpdateModel>.() -> Unit) {
        val obj = UpdateInit<AuthContext, IdType, ResourceType, UpdateModel>(
            UpdateModel::class
        )
        obj.init()
        this.updateInit = Left(obj)
    }

    inline fun <reified InputModel : Any> createOrUpdate(init: CreateOrUpdateInit<AuthContext, IdType, ResourceType, InputModel>.() -> Unit) {
        val obj = CreateOrUpdateInit<AuthContext, IdType, ResourceType, InputModel>(
            InputModel::class
        )
        obj.init()
        this.updateInit = Right(obj)
    }

    @PublishedApi
    internal var deleteInit: DeleteInit<AuthContext, IdType, ResourceType>? by setOnce()

    inline fun delete(init: DeleteInit<AuthContext, IdType, ResourceType>.() -> Unit) {
        //TODO: Error if already set
        val obj = DeleteInit<AuthContext, IdType, ResourceType>()
        obj.init()
        this.deleteInit = obj
    }

    @PublishedApi
    internal var readInit: ReadInit<AuthContext, IdType, ResourceType> by setOnce()

    inline fun read(init: ReadInit<AuthContext, IdType, ResourceType>.() -> Unit) {
        //TODO: Error if already set
        val obj = ReadInit<AuthContext, IdType, ResourceType>()
        obj.init()
        this.readInit = obj
    }

    @PublishedApi
    internal var listInit: Either<SimpleListInit<AuthContext, IdType, ResourceType, Any>, PagedCollectionInit<AuthContext, IdType, ResourceType, Any>>? by setOnce()

    inline fun <reified FilterType : Any> listSimple(init: SimpleListInit<AuthContext, IdType, ResourceType, FilterType>.() -> Unit) {
        val obj = SimpleListInit<AuthContext, IdType, ResourceType, FilterType>(
            FilterType::class
        )
        obj.init()
        @Suppress("UNCHECKED_CAST")
        this.listInit = Left(obj) as Either<SimpleListInit<AuthContext, IdType, ResourceType, Any>, PagedCollectionInit<AuthContext, IdType, ResourceType, Any>>
    }

    inline fun <reified FilterType : Any> listPaged(
        init: PagedCollectionInit<AuthContext, IdType, ResourceType, FilterType>.() -> Unit
    ) {
        val obj = PagedCollectionInit<AuthContext, IdType, ResourceType, FilterType>(
            FilterType::class
        )
        obj.init()
        @Suppress("UNCHECKED_CAST")
        this.listInit = Right(obj) as Either<SimpleListInit<AuthContext, IdType, ResourceType, Any>, PagedCollectionInit<AuthContext, IdType, ResourceType, Any>>
    }

    override fun toModel(context: ModelingContext): OperationModel<AuthContext, IdType, ResourceType> {
        return OperationModel(
            read = this.readInit.toModel(context),
            create = this.createInit?.toModel(context),
            update = this.updateInit?.fold({ it.toModel(context) }, { it.toModel(context) }),
            delete = this.deleteInit?.toModel(context),
            list = this.listInit?.fold({ it.toModel(context) }, { it.toModel(context) })
        )
    }
}

class ReadInit<AuthContext, IdType, ResourceModel> : DSLInit<ReadOperation<AuthContext, IdType, ResourceModel>>() {

    private var authorizer: ReadAuthorizer<AuthContext, IdType, ResourceModel> by setOnce()

    fun authorized(auth: ReadAuthorizer<AuthContext, IdType, ResourceModel>) {
        this.authorizer = auth
    }

    private var handler: ReadHandler<AuthContext, IdType, ResourceModel> by setOnce()

    fun handle(handler: ReadHandler<AuthContext, IdType, ResourceModel>) {
        this.handler = handler
    }

    override fun toModel(context: ModelingContext): ReadOperation<AuthContext, IdType, ResourceModel> {
        return ReadOperation(
            this.authorizer,
            this.handler
        )
    }

}

class SimpleListInit<AuthContext, IdType, ResourceModel, FilterType : Any>(
    private val filterType: KClass<FilterType>
) : DSLInit<SimpleListOperation<AuthContext, IdType, ResourceModel, FilterType>>() {

    private var handler: Either<
        ListHandler<AuthContext, FilterType, IdType>,
        ListHandler<AuthContext, FilterType, ResourceModel>
        > by setOnce()

    fun listIds(block: ListHandler<AuthContext, FilterType, IdType>) {
        handler = Left(block)
    }

    fun listObjects(block: ListHandler<AuthContext, FilterType, ResourceModel>) {
        handler = Right(block)
    }

    override fun toModel(context: ModelingContext): SimpleListOperation<AuthContext, IdType, ResourceModel, FilterType> {
        return SimpleListOperation(
            filterType = QueryParamModel(
                context.models.queryParamSchemaFor(filterType),
                context.models.queryParamReaderFor(filterType)
            ),
            handle = handler
        )
    }
}

class PagedCollectionInit<AuthContext, IdType, ResourceModel, FilterType : Any>(
    private val filterType: KClass<FilterType>
) : DSLInit<PagedListOperation<AuthContext, IdType, ResourceModel, FilterType>>() {
    var defaultSize: Int by setOnce()
    var maxSize: Int by setOnce()

    fun listIds(block: PagedListHandler<AuthContext, FilterType, IdType>) {
        this.handler = Left(block)
    }

    fun listObjects(block: PagedListHandler<AuthContext, FilterType, ResourceModel>) {
        this.handler = Right(block)
    }

    private var handler: Either<
        PagedListHandler<AuthContext, FilterType, IdType>,
        PagedListHandler<AuthContext, FilterType, ResourceModel>
        > by setOnce()

    override fun toModel(context: ModelingContext): PagedListOperation<AuthContext, IdType, ResourceModel, FilterType> {
        return PagedListOperation(
            filterType = QueryParamModel(
                context.models.queryParamSchemaFor(filterType),
                context.models.queryParamReaderFor(filterType)
            ),
            pageParamModel = QueryParamModel(
                context.models.queryParamSchemaFor(PagingParams::class),
                context.models.queryParamReaderFor(PagingParams::class)
            ),
            handle = handler,
            defaultPageSize = defaultSize,
            maxPageSize = maxSize
        )
    }
}

typealias ListHandler<AuthContext, FilterType, ResultType> =
    ListContext<AuthContext, FilterType>.() -> Collection<ResultType>

interface ListContext<AuthContext, FilterType> : AuthorizedContext<AuthContext> {
    val filters: FilterType
}

typealias PagedListHandler<AuthContext, FilterType, ResultType> =
    PagedListContext<AuthContext, FilterType>.() -> CollectionWithTotal<ResultType>

interface PagedListContext<AuthContext, FilterType> : ListContext<AuthContext, FilterType> {
    val paging: PagingParams
}

class CreateInit<AuthContext, IdType, CreateModel : Any>(
    private val input: KClass<CreateModel>
) : DSLInit<CreateOperation<AuthContext, IdType, CreateModel>>() {

    private var authorizationHandler: CreateAuthorizer<AuthContext, CreateModel> by setOnce()
    private var handler: CreateHandler<AuthContext, IdType, CreateModel> by setOnce()

    fun authorized(auth: CreateAuthorizer<AuthContext, CreateModel>) {
        authorizationHandler = auth
    }

    fun handle(handler: CreateHandler<AuthContext, IdType, CreateModel>) {
        this.handler = handler
    }

    override fun toModel(context: ModelingContext): CreateOperation<AuthContext, IdType, CreateModel> = CreateOperation(
        input = InputModel(
            input,
            context.models.jsonInputSchemaFor(input),
            context.models.jsonReaderFor(input)
        ),
        authorization = this.authorizationHandler,
        handle = this.handler
    )
}

class UpdateInit<AuthContext, IdType, ResourceModel, InputModel : Any>(
    private val input: KClass<InputModel>
) : DSLInit<UpdateOperation<AuthContext, IdType, ResourceModel, InputModel>>() {

    private var authorization: UpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel> by setOnce()
    private var handler: UpdateHandler<AuthContext, IdType, ResourceModel, InputModel> by setOnce()

    fun authorized(auth: UpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel>) {
        this.authorization = auth
    }

    fun handle(handler: UpdateHandler<AuthContext, IdType, ResourceModel, InputModel>) {
        this.handler = handler
    }

    override fun toModel(context: ModelingContext): SimpleUpdateOperation<AuthContext, IdType, ResourceModel, InputModel> {
        return SimpleUpdateOperation(
            input = InputModel(
                input,
                context.models.jsonInputSchemaFor(input),
                context.models.jsonReaderFor(input)
            ),
            authorization = this.authorization,
            handle = this.handler
        )
    }
}

class CreateOrUpdateInit<AuthContext, IdType, ResourceModel, InputModel : Any>(
    private val input: KClass<InputModel>
) : DSLInit<CreateOrUpdateOperation<AuthContext, IdType, ResourceModel, InputModel>>() {

    private var authorization: CreateOrUpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel> by setOnce()
    private var handler: CreateOrUpdateHandler<AuthContext, IdType, ResourceModel, InputModel> by setOnce()

    fun authorization(auth: CreateOrUpdateAuthorizer<AuthContext, IdType, ResourceModel, InputModel>) {
        this.authorization = auth
    }

    fun handle(handler: CreateOrUpdateHandler<AuthContext, IdType, ResourceModel, InputModel>) {
        this.handler = handler
    }

    override fun toModel(context: ModelingContext): CreateOrUpdateOperation<AuthContext, IdType, ResourceModel, InputModel> {
        return CreateOrUpdateOperation(
            input = InputModel(
                input,
                context.models.jsonInputSchemaFor(input),
                context.models.jsonReaderFor(input)
            ),
            authorization = this.authorization,
            handle = this.handler
        )
    }
}

class DeleteInit<AuthContext, IdType, ResourceModel>(
) : DSLInit<DeleteOperation<AuthContext, IdType, ResourceModel>>() {

    private var authorization: DeleteAuthorizer<AuthContext, IdType, ResourceModel> by setOnce()
    private var handler: DeleteHandler<AuthContext, IdType, ResourceModel> by setOnce()

    fun authorized(auth: DeleteAuthorizer<AuthContext, IdType, ResourceModel>) {
        this.authorization = auth
    }

    fun handle(handler: DeleteHandler<AuthContext, IdType, ResourceModel>) {
        this.handler = handler
    }

    override fun toModel(context: ModelingContext): DeleteOperation<AuthContext, IdType, ResourceModel> {
        return DeleteOperation(
            authorization = this.authorization,
            handle = this.handler
        )
    }
}

typealias Prop<Type> = KProperty1<*, Type>

interface InputValidator {
    fun validate(message: String, condition: () -> Boolean)

    fun isNotEmpty(prop: Prop<String>)


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


interface AuthorizedContext<AuthContext> {
    val authContext: AuthContext
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
