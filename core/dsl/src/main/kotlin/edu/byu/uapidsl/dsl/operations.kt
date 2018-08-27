package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.DslPart
import edu.byu.uapidsl.ModelingContext
import edu.byu.uapidsl.UApiMarker
import edu.byu.uapidsl.model.resource.*
import edu.byu.uapidsl.model.resource.identified.*
import edu.byu.uapidsl.model.resource.identified.ops.*
import edu.byu.uapidsl.model.resource.identified.ops.DeleteContext
import either.Either
import either.Left
import either.Right
import either.fold
import kotlin.reflect.KClass


class OperationsDSL<Auth : Any, Id : Any, Model : Any>(
    @PublishedApi internal val resource: ResourceDSL<Auth, Id, Model>
) : DslPart<IdentifiedResourceOperations<Auth, Id, Model>>() {

    @PublishedApi
    internal var createInit: CreateDSL<Auth, Id, *>? by setOnce()

    inline fun <reified CreateModel : Any> create(init: CreateDSL<Auth, Id, CreateModel>.() -> Unit) {
        val createInit = CreateDSL<Auth, Id, CreateModel>(
            CreateModel::class
        )
        createInit.init()
        this.createInit = createInit
    }

    @PublishedApi
    internal var updateInit: UpdateDSL<Auth, Id, Model, *>? by setOnce()

    inline fun <reified UpdateModel : Any> update(init: UpdateDSL<Auth, Id, Model, UpdateModel>.() -> Unit) {
        val obj = UpdateDSL<Auth, Id, Model, UpdateModel>(
            UpdateModel::class
        )
        obj.init()
        this.updateInit = obj
    }

    @PublishedApi
    internal var createOrUpdateInit: CreateOrUpdateDSL<Auth, Id, Model, *>? by setOnce()

    inline fun <reified InputModel : Any> createOrUpdate(init: CreateOrUpdateDSL<Auth, Id, Model, InputModel>.() -> Unit) {
        val obj = CreateOrUpdateDSL<Auth, Id, Model, InputModel>(
            InputModel::class
        )
        obj.init()
        this.createOrUpdateInit = obj
    }

    @PublishedApi
    internal var deleteInit: DeleteDSL<Auth, Id, Model>? by setOnce()

    inline fun delete(init: DeleteDSL<Auth, Id, Model>.() -> Unit) {
        //TODO: Error if already set
        val obj = DeleteDSL<Auth, Id, Model>()
        obj.init()
        this.deleteInit = obj
    }

    @PublishedApi
    internal var readInit: ReadDSL<Auth, Id, Model> by setOnce()

    inline fun read(init: ReadDSL<Auth, Id, Model>.() -> Unit) {
        //TODO: Error if already set
        val obj = ReadDSL(this.resource)
        obj.init()
        this.readInit = obj
    }

    @PublishedApi
    internal var listInit: Either<SimpleListDSL<Auth, Id, Model, Any>, PagedCollectionDSL<Auth, Id, Model, Any>>? by setOnce()

    inline fun <reified FilterType : Any> listSimple(init: SimpleListDSL<Auth, Id, Model, FilterType>.() -> Unit) {
        val obj = SimpleListDSL<Auth, Id, Model, FilterType>(
            FilterType::class
        )
        obj.init()
        @Suppress("UNCHECKED_CAST")
        this.listInit = Left(obj) as Either<SimpleListDSL<Auth, Id, Model, Any>, PagedCollectionDSL<Auth, Id, Model, Any>>
    }

    inline fun <reified FilterType : Any> listPaged(
        init: PagedCollectionDSL<Auth, Id, Model, FilterType>.() -> Unit
    ) {
        val obj = PagedCollectionDSL<Auth, Id, Model, FilterType>(
            FilterType::class
        )
        obj.init()
        @Suppress("UNCHECKED_CAST")
        this.listInit = Right(obj) as Either<SimpleListDSL<Auth, Id, Model, Any>, PagedCollectionDSL<Auth, Id, Model, Any>>
    }

    override fun toModel(context: ModelingContext): IdentifiedResourceOperations<Auth, Id, Model> {
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

class ReadDSL<Auth : Any, Id : Any, Model : Any>(
    private val resource: ResourceDSL<Auth, Id, Model>
) : DslPart<ReadOperation<Auth, Id, Model>>() {

    private var authorizer: Authorizer<IdentifiedReadContext<Auth, Id, Model>> by setOnce()

    fun canUserView(auth: Authorizer<IdentifiedReadContext<Auth, Id, Model>>) {
        this.authorizer = auth
    }

    private var handler: Loader<IdentifiedLoadContext<Auth, Id>, Model> by setOnce()

    fun loadModel(handler: Loader<IdentifiedLoadContext<Auth, Id>, Model>) {
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

    fun canUserCreate(auth: CreateAuthorizer<AuthContext, CreateModel>) {
        authorizationHandler = auth
    }

    fun handleCreate(handler: CreateHandler<AuthContext, IdType, CreateModel>) {
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

interface UpdatePossibleContext<Id : Any, Model : Any> {
    val resource: IdentifiedResourceModelContext<Id, Model>
}

class UpdateDSL<Auth : Any, Id : Any, Model : Any, Input : Any>(
    private val input: KClass<Input>
) : DslPart<SimpleUpdateOperation<Auth, Id, Model, Input>>() {

    private var authorization: Authorizer<IdentifiedUpdateContext<Auth, Id, Model, Input>> by setOnce()
    private var handler: Handler<IdentifiedUpdateContext<Auth, Id, Model, Input>> by setOnce()
    private var validate: Validator<IdentifiedUpdateValidationContext<Auth, Id, Model, Input>> by setOnce()

    fun canUserUpdate(auth: Authorizer<IdentifiedUpdateContext<Auth, Id, Model, Input>>) {
        this.authorization = auth
    }

    fun handleUpdate(handler: Handler<IdentifiedUpdateContext<Auth, Id, Model, Input>>) {
        this.handler = handler
    }

    fun validateInput(validator: Validator<IdentifiedUpdateValidationContext<Auth, Id, Model, Input>>) {
        this.validate = validator
    }

    fun canBeUpdated(func: MutationPossibleChecker<UpdatePossibleContext<Id, Model>>) {}

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

class CreateOrUpdateDSL<Auth : Any, Id : Any, Model : Any, Input : Any>(
    private val input: KClass<Input>
) : DslPart<CreateOrUpdateOperation<Auth, Id, Model, Input>>() {

    private var authorization: Authorizer<IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input>> by setOnce()
    private var handler: Handler<IdentifiedCreateOrUpdateContext<Auth, Id, Model, Input>> by setOnce()
    private var validator: Validator<IdentifiedCreateOrUpdateValidationContext<Auth, Id, Model, Input>> by setOnce()

    fun create(block: CreateDSL<Auth, Id, Input>.() -> Unit) {
    }
    fun update(block: UpdateDSL<Auth, Id, Model, Input>.() -> Unit) {
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

    @UApiMarker
    class CreateDSL<Auth : Any, Id : Any, Input : Any> {
        fun canUserCreate(auth: Authorizer<CreateWithIdContext<Auth, Id, Input>>) {

        }

        fun handleCreate(block: Handler<CreateWithIdContext<Auth, Id, Input>>) {

        }
    }

    @UApiMarker
    class UpdateDSL<Auth : Any, Id : Any, Model : Any, Input : Any> {

        fun canUserUpdate(auth: Authorizer<IdentifiedUpdateContext<Auth, Id, Model, Input>>) {
        }

        fun handleUpdate(handler: Handler<IdentifiedUpdateContext<Auth, Id, Model, Input>>) {
        }

        fun canBeUpdated(func: MutationPossibleChecker<UpdatePossibleContext<Id, Model>>) {}

    }
}

class DeleteDSL<Auth : Any, Id : Any, Model : Any>(
) : DslPart<DeleteOperation<Auth, Id, Model>>() {

    private var authorization: Authorizer<DeleteContext<Auth, Id, Model>> by setOnce()
    private var handler: Handler<DeleteContext<Auth, Id, Model>> by setOnce()

    fun canUserDelete(auth: Authorizer<DeleteContext<Auth, Id, Model>>) {
        this.authorization = auth
    }

    fun handleDelete(handler: Handler<DeleteContext<Auth, Id, Model>>) {
        this.handler = handler
    }

    fun canBeDeleted(auth: MutationPossibleChecker<DeleteContext<Auth, Id, Model>>) {

    }

    override fun toModel(context: ModelingContext): DeleteOperation<Auth, Id, Model> {
        return DeleteOperation(
            authorized = this.authorization,
            handle = this.handler
        )
    }
}


