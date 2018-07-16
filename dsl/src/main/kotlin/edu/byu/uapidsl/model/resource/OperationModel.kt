package edu.byu.uapidsl.model.resource

import com.fasterxml.jackson.databind.ObjectReader
import edu.byu.uapidsl.dsl.*
import edu.byu.uapidsl.model.resource.ops.ListOperation
import edu.byu.uapidsl.model.resource.ops.PagedListOperation
import edu.byu.uapidsl.model.resource.ops.SimpleListOperation
import edu.byu.uapidsl.typemodeling.*
import edu.byu.uapidsl.types.*
import kotlin.reflect.KClass

data class OperationModel<AuthContext, IdType, ResourceType>(
    val read: ReadOperation<AuthContext, IdType, ResourceType>,
//    val list: ListOperation<AuthContext, IdType, ResourceType, *, *, *, *>?,
    val list: ListOperation<AuthContext, IdType, ResourceType, *, *, *, *>?,
    val create: CreateOperation<AuthContext, IdType, *>?,
    val update: UpdateOperation<AuthContext, IdType, ResourceType, *>?,
//    val update: Either<
//        SimpleUpdateOperation<AuthContext, IdType, ResourceType, *>,
//        CreateOrUpdateOperation<AuthContext, IdType, ResourceType, *>
//        >?,
    val delete: DeleteOperation<AuthContext, IdType, ResourceType>?
) {

    val listable: Boolean = list != null
    val listStyle: ListStyle? = when (list) {
        null -> null
        is SimpleListOperation -> TODO()
        is PagedListOperation -> TODO()
    }

    val updatable: Boolean = update != null
    val updateStyle: UpdateStyle? = when (update) {
        null -> null
        is SimpleUpdateOperation -> UpdateStyle.SIMPLE
        is CreateOrUpdateOperation -> UpdateStyle.UPSERT
    }

    val creatable: Boolean = create != null || updateStyle == UpdateStyle.UPSERT
    val createStyle: CreateStyle? = when {
        create != null -> CreateStyle.SIMPLE
        updateStyle == UpdateStyle.UPSERT -> CreateStyle.UPSERT
        else -> null
    }

    val deletable: Boolean = delete != null


}

enum class UpdateStyle {
    SIMPLE, UPSERT
}

enum class CreateStyle {
    SIMPLE, UPSERT
}

enum class ListStyle {
    PAGED, SIMPLE
}

data class IdModel<Type : Any>(
    val schema: PathParamSchema<*>,
    val reader: PathParamReader<Type>
)

data class ReadOperation<AuthContext, IdType, DomainType>(
    val authorization: ReadAuthorizer<AuthContext, IdType, DomainType>,
    val handle: ReadHandler<AuthContext, IdType, DomainType>
): DomainModelOps<AuthContext, IdType, DomainType> {

    override fun modelToResult(authContext: AuthContext, model: DomainType): UAPIResponse<*> {
        TODO("not implemented")
    }

    override fun idToModel(authContext: AuthContext, id: IdType): DomainType? {
        TODO("not implemented")
    }
}

data class ReadLoadContextImpl<AuthContext, IdType>(
    override val authContext: AuthContext,
    override val id: IdType
) : ReadLoadContext<AuthContext, IdType>


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

data class QueryParamModel<Type : Any>(
    val schema: QueryParamSchema,
    val reader: QueryParamReader<Type>
)


