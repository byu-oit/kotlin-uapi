package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.DslPart
import edu.byu.uapidsl.ModelingContext
import edu.byu.uapidsl.model.resource.*
import edu.byu.uapidsl.model.resource.identified.*
import edu.byu.uapidsl.model.resource.identified.ops.*
import edu.byu.uapidsl.model.resource.identified.ops.DeleteContext
import either.Either
import either.Left
import either.Right
import either.fold
import kotlin.reflect.KClass


class OperationsDSL<AuthContext: Any, IdType: Any, ResourceType: Any>(
    @PublishedApi internal val resource: ResourceDSL<AuthContext, IdType, ResourceType>
): DslPart<IdentifiedResourceOperations<AuthContext, IdType, ResourceType>>() {

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
    internal var updateInit: UpdateDSL<AuthContext, IdType, ResourceType, *>? by setOnce()

    inline fun <reified UpdateModel : Any> update(init: UpdateDSL<AuthContext, IdType, ResourceType, UpdateModel>.() -> Unit) {
        val obj = UpdateDSL<AuthContext, IdType, ResourceType, UpdateModel>(
            UpdateModel::class
        )
        obj.init()
        this.updateInit = obj
    }

    @PublishedApi
    internal var createOrUpdateInit: CreateOrUpdateDSL<AuthContext, IdType, ResourceType, *>? by setOnce()

    inline fun <reified InputModel : Any> createOrUpdate(init: CreateOrUpdateDSL<AuthContext, IdType, ResourceType, InputModel>.() -> Unit) {
        val obj = CreateOrUpdateDSL<AuthContext, IdType, ResourceType, InputModel>(
            InputModel::class
        )
        obj.init()
        this.createOrUpdateInit = obj
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

    override fun toModel(context: ModelingContext): IdentifiedResourceOperations<AuthContext, IdType, ResourceType> {
        return IdentifiedResourceOperations(
            read = this.readInit.toModel(context),
            create = this.createInit?.toModel(context),
            update = this.updateInit?.toModel(context),
            createOrUpdate = this.createOrUpdateInit?.toModel(context),
            delete = this.deleteInit?.toModel(context),
            list = this.listInit?.fold({ it.toModel(context) }, { it.toModel(context) })
        )
    }
}

class ReadDSL<Auth: Any, Id: Any, Model: Any>(
    private val resource: ResourceDSL<Auth, Id, Model>
) : DslPart<ReadOperation<Auth, Id, Model>>() {

    private var authorizer: Authorizer<IdentifiedReadContext<Auth, Id, Model>> by setOnce()

    fun authorized(auth: Authorizer<IdentifiedReadContext<Auth, Id, Model>>) {
        this.authorizer = auth
    }

    private var handler: Loader<IdentifiedLoadContext<Auth, Id>, Model> by setOnce()

    fun handle(handler: Loader<IdentifiedLoadContext<Auth, Id>, Model>) {
        this.handler = handler
    }

    override fun toModel(context: ModelingContext): ReadOperation<Auth, Id, Model> {
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

    private var authorizationHandler: Authorizer<CreateContext<AuthContext, CreateModel>> by setOnce()
    private var handler: CreateHandler<AuthContext, IdType, CreateModel> by setOnce()
    private var validator: Validator<CreateValidationContext<AuthContext, CreateModel>> by setOnce()

    fun authorized(auth: CreateAuthorizer<AuthContext, CreateModel>) {
        authorizationHandler = auth
    }

    fun handle(handler: CreateHandler<AuthContext, IdType, CreateModel>) {
        this.handler = handler
    }

    fun validateInput(validator: Validator<CreateValidationContext<AuthContext, CreateModel>>) {
        this.validator = validator
    }

    override fun toModel(context: ModelingContext): CreateOperation<AuthContext, IdType, CreateModel> = CreateOperation(
        input = InputModel(
            input,
            context.models.jsonInputSchemaFor(input),
            context.models.jsonReaderFor(input)
        ),
        authorization = this.authorizationHandler,
        handle = this.handler,
        validator = this.validator
    )
}

class UpdateDSL<Auth: Any, Id: Any, Model: Any, Input : Any>(
    private val input: KClass<Input>
) : DslPart<SimpleUpdateOperation<Auth, Id, Model, Input>>() {

    private var authorization: Authorizer<IdentifiedUpdateContext<Auth, Id, Model, Input>> by setOnce()
    private var handler: Handler<IdentifiedUpdateContext<Auth, Id, Model, Input>> by setOnce()
    private var validate: Validator<IdentifiedUpdateValidationContext<Auth, Id, Model, Input>> by setOnce()

    fun authorized(auth: Authorizer<IdentifiedUpdateContext<Auth, Id, Model, Input>>) {
        this.authorization = auth
    }

    fun handle(handler: Handler<IdentifiedUpdateContext<Auth, Id, Model, Input>>) {
        this.handler = handler
    }

    fun validateInput(validator: Validator<IdentifiedUpdateValidationContext<Auth, Id, Model, Input>>) {
        this.validate = validator
    }

    override fun toModel(context: ModelingContext): SimpleUpdateOperation<Auth, Id, Model, Input> {
        return SimpleUpdateOperation(
            input = InputModel(
                input,
                context.models.jsonInputSchemaFor(input),
                context.models.jsonReaderFor(input)
            ),
            authorized = this.authorization,
            handle = this.handler,
            validate = this.validate
        )
    }
}

class CreateOrUpdateDSL<Auth: Any, Id: Any, Model: Any, Input : Any>(
    private val input: KClass<Input>
) : DslPart<CreateOrUpdateOperation<Auth, Id, Model, Input>>() {

    private var authorization: Authorizer<IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input>> by setOnce()
    private var handler: Handler<IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input>> by setOnce()
    private var validator: Validator<IdentifiedCreateOrUpdateValidationContext<Auth, Id, Model, Input>> by setOnce()

    fun authorized(auth: Authorizer<IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input>>) {
        this.authorization = auth
    }

    fun handle(handler: Handler<IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input>>) {
        this.handler = handler
    }

    fun validateInput(validator: Validator<IdentifiedCreateOrUpdateValidationContext<Auth, Id, Model, Input>>) {
        this.validator = validator
    }

    override fun toModel(context: ModelingContext): CreateOrUpdateOperation<Auth, Id, Model, Input> {
        return CreateOrUpdateOperation(
            input = InputModel(
                input,
                context.models.jsonInputSchemaFor(input),
                context.models.jsonReaderFor(input)
            ),
            authorized = this.authorization,
            handle = this.handler,
            validate = validator
        )
    }
}

class DeleteDSL<Auth: Any, Id: Any, Model: Any>(
) : DslPart<DeleteOperation<Auth, Id, Model>>() {

    private var authorization: Authorizer<DeleteContext<Auth, Id, Model>> by setOnce()
    private var handler: Handler<DeleteContext<Auth, Id, Model>> by setOnce()

    fun authorized(auth: Authorizer<DeleteContext<Auth, Id, Model>>) {
        this.authorization = auth
    }

    fun handle(handler: Handler<DeleteContext<Auth, Id, Model>>) {
        this.handler = handler
    }

    override fun toModel(context: ModelingContext): DeleteOperation<Auth, Id, Model> {
        return DeleteOperation(
            authorized = this.authorization,
            handle = this.handler
        )
    }
}


