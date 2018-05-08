package edu.byu.uapidsl.model

import edu.byu.uapidsl.dsl.*
import either.Either

data class ResourceModel<AuthContext, IdType : Any, ResourceType : Any>(
    val type: Introspectable<ResourceType>,
    val idModel: PathIdentifierModel<IdType>,
    val name: String,
    val read: ReadOperation<AuthContext, IdType, ResourceType>,
    val list: ListOperation<AuthContext, IdType, ResourceType, *>?,
    val create: CreateOperation<AuthContext, IdType, *>?,
    val update: UpdateOperation<AuthContext, IdType, ResourceType, *>?,
    val delete: DeleteOperation<AuthContext, IdType, ResourceType>?,
    val output: OutputModel<AuthContext, IdType, ResourceType, *>//,
//  val subresources: List<SubResourceModel<AuthContext, IdType, ResourceType, Any>>
) {
    init {

    }
}

data class OutputModel<AuthContext, IdType, DomainType, OutputType : Any>(
    val type: Introspectable<OutputType>,
    val example: OutputType,
    val handle: TransformModelHandler<AuthContext, IdType, DomainType, OutputType>
)

data class ReadOperation<AuthContext, IdType, DomainType>(
    val authorization: ReadAuthorizer<AuthContext, IdType, DomainType>,
    val handle: ReadHandler<AuthContext, IdType, DomainType>
)

sealed class UpdateOperation<AuthContext, IdType, DomainType, InputType : Any> {
    abstract val input: Introspectable<InputType>
}

data class SimpleUpdateOperation<AuthContext, IdType, DomainType, InputType : Any>(
    override val input: Introspectable<InputType>,
    val authorization: UpdateAuthorizer<AuthContext, IdType, DomainType, InputType>,
    val handle: UpdateHandler<AuthContext, IdType, DomainType, InputType>
) : UpdateOperation<AuthContext, IdType, DomainType, InputType>()

data class CreateOrUpdateOperation<AuthContext, IdType, DomainType, InputType : Any>(
    override val input: Introspectable<InputType>,
    val authorization: CreateOrUpdateAuthorizer<AuthContext, IdType, DomainType, InputType>,
    val handle: CreateOrUpdateHandler<AuthContext, IdType, DomainType, InputType>
) : UpdateOperation<AuthContext, IdType, DomainType, InputType>()

data class CreateOperation<AuthContext, IdType, InputType : Any>(
    val input: Introspectable<InputType>,
    val authorization: CreateAuthorizer<AuthContext, InputType>,
    val handle: CreateHandler<AuthContext, IdType, InputType>
)

data class DeleteOperation<AuthContext, IdType, DomainType>(
    val authorization: DeleteAuthorizer<AuthContext, IdType, DomainType>,
    val handle: DeleteHandler<AuthContext, IdType, DomainType>
)

interface ListOperation<AuthContext, IdType, DomainType, Filters : Any>

data class SimpleListOperation<AuthContext, IdType, DomainType, Filters : Any>(
    val filterType: Introspectable<Filters>,
    val handle: Either<
        ListHandler<AuthContext, Filters, IdType>,
        ListHandler<AuthContext, Filters, DomainType>
        >
) : ListOperation<AuthContext, IdType, DomainType, Filters>

data class PagedListOperation<AuthContext, IdType, DomainType, Filters : Any>(
    val filterType: Introspectable<Filters>,
    val handle: Either<
        PagedListHandler<AuthContext, Filters, IdType>,
        PagedListHandler<AuthContext, Filters, DomainType>
        >
) : ListOperation<AuthContext, IdType, DomainType, Filters>

