package edu.byu.uapidsl.model

import edu.byu.uapidsl.dsl.*
import either.Either
import kotlin.reflect.KClass

data class SubResourceModel<AuthContext : Any, SubId : Any, SubModel : Any>(
    val type: KClass<SubModel>,
    val name: String,
    val read: ReadOperation<AuthContext, SubId, SubModel>,
    val list: ListOperation<AuthContext, SubId, SubModel, Any>?,
    val create: CreateOperation<AuthContext, SubId, Any>?,
    val update: UpdateOperation<AuthContext, SubId, SubModel, Any>?,
    val delete: DeleteOperation<AuthContext, SubId, SubModel>?
//    val example: SubModel,
//    val transform: TransformModel<AuthContext, SubId, SubModel, Any>
) {
    init {

    }
}

data class SubTransformModel<AuthContext, SubId, SubModel, OutputType : Any>(
    val type: KClass<OutputType>,
    val handle: TransformModelHandler<AuthContext, SubId, SubModel, OutputType>
)

data class SubReadOperation<AuthContext : Any, SubId : Any, SubModel : Any>(
    val authorization: ReadAuthorizer<AuthContext, SubId, SubModel>,
    val handle: ReadHandler<AuthContext, SubId, SubModel>
)

interface SubUpdateOperation<AuthContext, SubId, SubModel, InputType : Any> {
    val input: KClass<InputType>
}

data class SubSimpleUpdateOperation<AuthContext, SubId, SubModel, InputType : Any>(
    override val input: KClass<InputType>,
    val authorization: UpdateAuthorizer<AuthContext, SubId, SubModel, InputType>,
    val handle: UpdateHandler<AuthContext, SubId, SubModel, InputType>
) : SubUpdateOperation<AuthContext, SubId, SubModel, InputType>

data class SubCreateOrUpdateOperation<AuthContext, SubId, SubModel, InputType : Any>(
    override val input: KClass<InputType>,
    val authorization: CreateOrUpdateAuthorizer<AuthContext, SubId, SubModel, InputType>,
    val handle: CreateOrUpdateHandler<AuthContext, SubId, SubModel, InputType>
) : SubUpdateOperation<AuthContext, SubId, SubModel, InputType>

data class SubCreateOperation<AuthContext, SubId, SubModel, InputType : Any>(
    val input: KClass<InputType>,
    val authorization: CreateAuthorizer<AuthContext, InputType>,
    val handle: CreateHandler<AuthContext, SubId, InputType>
)

data class SubDeleteOperation<AuthContext, SubId, SubModel>(
    val authorization: DeleteAuthorizer<AuthContext, SubId, SubModel>,
    val handle: DeleteHandler<AuthContext, SubId, SubModel>
)

interface SubListOperation<AuthContext, SubId, SubModel, Filters : Any>

data class SubSimpleListOperation<AuthContext, SubId, SubModel, Filters : Any>(
    val filterType: KClass<Filters>,
    val handle: Either<
        ListHandler<AuthContext, Filters, SubId>,
        ListHandler<AuthContext, Filters, SubModel>
        >
) : SubListOperation<AuthContext, SubId, SubModel, Filters>

data class SubPagedListOperation<AuthContext, SubId, SubModel, Filters : Any>(
    val filterType: KClass<Filters>,
    val handle: Either<
        PagedListHandler<AuthContext, Filters, SubId>,
        PagedListHandler<AuthContext, Filters, SubModel>
        >
) : SubListOperation<AuthContext, SubId, SubModel, Filters>

