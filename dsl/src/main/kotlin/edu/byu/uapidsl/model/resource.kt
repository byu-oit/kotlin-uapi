package edu.byu.uapidsl.model

import com.fasterxml.jackson.databind.ObjectReader
import edu.byu.uapidsl.dsl.*
import edu.byu.uapidsl.typemodeling.*
import edu.byu.uapidsl.types.UAPIField
import either.Either
import kotlin.reflect.KClass

interface ModelResponseMapper<AuthContext, IdType, ModelType> {
    fun mapResponse(authContext: AuthContext, idType: IdType, modelType: ModelType): Map<String, UAPIField<*>>
}

data class ResourceModel<AuthContext, IdType : Any, ResourceType : Any>(
    val type: KClass<ResourceType>,
    val responseModel: ResponseModel<ResourceType>,
    val idModel: IdModel<IdType>,
    val idExtractor: IdExtractor<IdType, ResourceType>,
    val name: String,
    val example: ResourceType,
    val operations: OperationModel<AuthContext, IdType, ResourceType>
//    val output: OutputModel<AuthContext, IdType, ResourceType, *>,
//    val responseMapper: ModelResponseMapper<AuthContext, IdType, ResourceType>
//  val subresources: List<SubResourceModel<AuthContext, IdType, ResourceType, Any>>
) {
    init {

    }
}

data class OperationModel<AuthContext, IdType, ResourceType>(
    val read: ReadOperation<AuthContext, IdType, ResourceType>,
    val list: ListOperation<AuthContext, IdType, ResourceType, *>?,
    val create: CreateOperation<AuthContext, IdType, *>?,
    val update: UpdateOperation<AuthContext, IdType, ResourceType, *>?,
//    val update: Either<
//        SimpleUpdateOperation<AuthContext, IdType, ResourceType, *>,
//        CreateOrUpdateOperation<AuthContext, IdType, ResourceType, *>
//        >?,
    val delete: DeleteOperation<AuthContext, IdType, ResourceType>?
)

data class IdModel<Type : Any>(
    val schema: PathParamSchema<*>,
    val reader: PathParamReader<Type>
)

data class ReadOperation<AuthContext, IdType, DomainType>(
    val authorization: ReadAuthorizer<AuthContext, IdType, DomainType>,
    val handle: ReadHandler<AuthContext, IdType, DomainType>
)

sealed class UpdateOperation<AuthContext, IdType, DomainType, InputType : Any> {
    abstract val input: InputModel<InputType>
}

data class InputModel<Type : Any>(
    val type: KClass<Type>,
    val schema: JsonInputSchema,
    val reader: ObjectReader
)

data class SimpleUpdateOperation<AuthContext, IdType, DomainType, InputType : Any>(
    override val input: InputModel<InputType>,
    val authorization: UpdateAuthorizer<AuthContext, IdType, DomainType, InputType>,
    val handle: UpdateHandler<AuthContext, IdType, DomainType, InputType>
) : UpdateOperation<AuthContext, IdType, DomainType, InputType>()

data class CreateOrUpdateOperation<AuthContext, IdType, DomainType, InputType : Any>(
    override val input: InputModel<InputType>,
    val authorization: CreateOrUpdateAuthorizer<AuthContext, IdType, DomainType, InputType>,
    val handle: CreateOrUpdateHandler<AuthContext, IdType, DomainType, InputType>
) : UpdateOperation<AuthContext, IdType, DomainType, InputType>()

data class CreateOperation<AuthContext, IdType, InputType : Any>(
    val input: InputModel<InputType>,
    val authorization: CreateAuthorizer<AuthContext, InputType>,
    val handle: CreateHandler<AuthContext, IdType, InputType>
)

data class DeleteOperation<AuthContext, IdType, DomainType>(
    val authorization: DeleteAuthorizer<AuthContext, IdType, DomainType>,
    val handle: DeleteHandler<AuthContext, IdType, DomainType>
)

sealed class ListOperation<AuthContext, IdType, DomainType, Filters : Any>

data class SimpleListOperation<AuthContext, IdType, DomainType, Filters : Any>(
    val filterType: QueryParamModel<Filters>,
    val handle: Either<
        ListHandler<AuthContext, Filters, IdType>,
        ListHandler<AuthContext, Filters, DomainType>
        >
) : ListOperation<AuthContext, IdType, DomainType, Filters>()

data class QueryParamModel<Type : Any>(
    val schema: QueryParamSchema,
    val reader: QueryParamReader<Type>
)

data class PagedListOperation<AuthContext, IdType, DomainType, Filters : Any>(
    val filterType: QueryParamModel<Filters>,
    val pageParamModel: QueryParamModel<PagingParams>,
    val defaultPageSize: Int,
    val maxPageSize: Int,
    val handle: Either<
        PagedListHandler<AuthContext, Filters, IdType>,
        PagedListHandler<AuthContext, Filters, DomainType>
        >
) : ListOperation<AuthContext, IdType, DomainType, Filters>()

