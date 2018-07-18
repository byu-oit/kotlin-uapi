package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.DslPart
import edu.byu.uapidsl.ModelingContext
import edu.byu.uapidsl.model.resource.*
import edu.byu.uapidsl.model.resource.ops.*
import either.Either
import either.Left
import either.Right
import either.fold
import kotlin.reflect.KClass


class OperationsDSL<AuthContext: Any, IdType: Any, ResourceType: Any>(
    @PublishedApi internal val resource: ResourceDSL<AuthContext, IdType, ResourceType>
): DslPart<OperationModel<AuthContext, IdType, ResourceType>>() {

    @PublishedApi
    internal var createInit: CreateDSL<AuthContext, IdType, *>? by setOnce()

    inline fun <reified CreateModel : Any> create(init: CreateDSL<AuthContext, IdType, CreateModel>.() -> Unit) {
        val createInit = CreateDSL<AuthContext, IdType, CreateModel>(
            CreateModel::class
        )
        createInit.init()
        this.createInit = createInit
    }

    @PublishedApi
    internal var updateInit: Either<UpdateDSL<AuthContext, IdType, ResourceType, *>, CreateOrUpdateDSL<AuthContext, IdType, ResourceType, *>>? by setOnce()

    inline fun <reified UpdateModel : Any> update(init: UpdateDSL<AuthContext, IdType, ResourceType, UpdateModel>.() -> Unit) {
        val obj = UpdateDSL<AuthContext, IdType, ResourceType, UpdateModel>(
            UpdateModel::class
        )
        obj.init()
        this.updateInit = Left(obj)
    }

    inline fun <reified InputModel : Any> createOrUpdate(init: CreateOrUpdateDSL<AuthContext, IdType, ResourceType, InputModel>.() -> Unit) {
        val obj = CreateOrUpdateDSL<AuthContext, IdType, ResourceType, InputModel>(
            InputModel::class
        )
        obj.init()
        this.updateInit = Right(obj)
    }

    @PublishedApi
    internal var deleteInit: DeleteDSL<AuthContext, IdType, ResourceType>? by setOnce()

    inline fun delete(init: DeleteDSL<AuthContext, IdType, ResourceType>.() -> Unit) {
        //TODO: Error if already set
        val obj = DeleteDSL<AuthContext, IdType, ResourceType>()
        obj.init()
        this.deleteInit = obj
    }

    @PublishedApi
    internal var readInit: ReadDSL<AuthContext, IdType, ResourceType> by setOnce()

    inline fun read(init: ReadDSL<AuthContext, IdType, ResourceType>.() -> Unit) {
        //TODO: Error if already set
        val obj = ReadDSL(this.resource)
        obj.init()
        this.readInit = obj
    }

    @PublishedApi
    internal var listInit: Either<SimpleListDSL<AuthContext, IdType, ResourceType, Any>, PagedCollectionDSL<AuthContext, IdType, ResourceType, Any>>? by setOnce()

    inline fun <reified FilterType : Any> listSimple(init: SimpleListDSL<AuthContext, IdType, ResourceType, FilterType>.() -> Unit) {
        val obj = SimpleListDSL<AuthContext, IdType, ResourceType, FilterType>(
            FilterType::class
        )
        obj.init()
        @Suppress("UNCHECKED_CAST")
        this.listInit = Left(obj) as Either<SimpleListDSL<AuthContext, IdType, ResourceType, Any>, PagedCollectionDSL<AuthContext, IdType, ResourceType, Any>>
    }

    inline fun <reified FilterType : Any> listPaged(
        init: PagedCollectionDSL<AuthContext, IdType, ResourceType, FilterType>.() -> Unit
    ) {
        val obj = PagedCollectionDSL<AuthContext, IdType, ResourceType, FilterType>(
            FilterType::class
        )
        obj.init()
        @Suppress("UNCHECKED_CAST")
        this.listInit = Right(obj) as Either<SimpleListDSL<AuthContext, IdType, ResourceType, Any>, PagedCollectionDSL<AuthContext, IdType, ResourceType, Any>>
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

class ReadDSL<AuthContext: Any, IdType: Any, ResourceModel: Any>(
    private val resource: ResourceDSL<AuthContext, IdType, ResourceModel>
) : DslPart<ReadOperation<AuthContext, IdType, ResourceModel>>() {

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
            this.handler,
            this.resource.idFromModel
        )
    }

}

class SimpleListDSL<AuthContext, IdType, ResourceModel, FilterType : Any>(
    private val filterType: KClass<FilterType>
) : DslPart<SimpleListOperation<AuthContext, IdType, ResourceModel, FilterType>>() {

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

class PagedCollectionDSL<AuthContext, IdType, ResourceModel, FilterType : Any>(
    private val filterType: KClass<FilterType>
) : DslPart<PagedListOperation<AuthContext, IdType, ResourceModel, FilterType>>() {
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

class CreateDSL<AuthContext, IdType, CreateModel : Any>(
    private val input: KClass<CreateModel>
) : DslPart<CreateOperation<AuthContext, IdType, CreateModel>>() {

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

class UpdateDSL<AuthContext, IdType, ResourceModel, InputModel : Any>(
    private val input: KClass<InputModel>
) : DslPart<SimpleUpdateOperation<AuthContext, IdType, ResourceModel, InputModel>>() {

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

class CreateOrUpdateDSL<AuthContext, IdType, ResourceModel, InputModel : Any>(
    private val input: KClass<InputModel>
) : DslPart<CreateOrUpdateOperation<AuthContext, IdType, ResourceModel, InputModel>>() {

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

class DeleteDSL<AuthContext, IdType, ResourceModel>(
) : DslPart<DeleteOperation<AuthContext, IdType, ResourceModel>>() {

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


