package edu.byu.uapidsl.model.resource

import com.fasterxml.jackson.databind.ObjectReader
import edu.byu.uapidsl.model.resource.ops.*
import edu.byu.uapidsl.typemodeling.*
import kotlin.reflect.KClass

data class OperationModel<AuthContext, IdType, ResourceType: Any>(
    val read: ReadOperation<AuthContext, IdType, ResourceType>,
    val list: ListOperation<AuthContext, IdType, ResourceType, *, *, *, *>?,
    val create: CreateOperation<AuthContext, IdType, *>?,
    val update: UpdateOperation<AuthContext, IdType, ResourceType, *, *>?,
    val delete: DeleteOperation<AuthContext, IdType, ResourceType>?
) {

    val listable: Boolean = list != null
    val listStyle: ListStyle? = when (list) {
        null -> null
        is SimpleListOperation -> ListStyle.SIMPLE
        is PagedListOperation -> ListStyle.PAGED
    }

    val updatable: Boolean = update != null
    val updateStyle: UpdateStyle? = when (update) {
        null -> null
        is SimpleUpdateOperation<*, *, *, *> -> UpdateStyle.SIMPLE
        is CreateOrUpdateOperation<*, *, *, *> -> UpdateStyle.UPSERT
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

data class InputModel<Type : Any>(
    val type: KClass<Type>,
    val schema: JsonInputSchema,
    val reader: ObjectReader
)

data class QueryParamModel<Type : Any>(
    val schema: QueryParamSchema,
    val reader: QueryParamReader<Type>
)


