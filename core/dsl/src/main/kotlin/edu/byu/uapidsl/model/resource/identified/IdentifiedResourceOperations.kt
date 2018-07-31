package edu.byu.uapidsl.model.resource.identified

import com.fasterxml.jackson.databind.ObjectReader
import edu.byu.uapidsl.model.resource.ResourceOperations
import edu.byu.uapidsl.model.resource.identified.ops.*
import edu.byu.uapidsl.typemodeling.*
import kotlin.reflect.KClass

data class IdentifiedResourceOperations<AuthContext: Any, Id: Any, Model: Any>(
    override val read: ReadOperation<AuthContext, Id, Model>,
    val list: ListOperation<AuthContext, Id, Model, *, *, *, *>?,
    val create: CreateOperation<AuthContext, Id, *>?,
    override val update: SimpleUpdateOperation<AuthContext, Id, Model, *>?,
    override val createOrUpdate: CreateOrUpdateOperation<AuthContext, Id, Model, *>?,
    override val delete: DeleteOperation<AuthContext, Id, Model>?
): ResourceOperations<
    AuthContext,
    Model,
    IdentifiedResourceModelContext<Id, Model>,
    IdentifiedResourceOptionalModelContext<Id, Model>> {

    val listable: Boolean = list != null
    val listStyle: ListStyle? = when (list) {
        null -> null
        is SimpleListOperation -> ListStyle.SIMPLE
        is PagedListOperation -> ListStyle.PAGED
    }

    val updatable: Boolean = update != null
    val updateStyle: UpdateStyle? = when {
        create != null -> UpdateStyle.SIMPLE
        update != null -> UpdateStyle.UPSERT
        else -> null
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


