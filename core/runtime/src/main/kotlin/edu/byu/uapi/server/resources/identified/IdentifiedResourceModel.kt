package edu.byu.uapi.server.resources.identified

import edu.byu.uapi.server.schemas.*

data class IdentifiedResourceModel(
    val name: String,
    val identifier: IdentifierModel,
    val responseModel: ResponseModel,
    val mutations: IdentifiedResourceMutations,
    val listViewModel: ListViewModel? = null
)

data class IdentifiedResourceMutations(
    val create: CreateOperationModel?,
    val update: UpdateOperationModel?,
    val delete: DeleteOperationModel?
)

data class CreateOperationModel(
    val schema: InputSchema,
    val docs: String? = null
)

data class UpdateOperationModel(
    val schema: InputSchema,
    val allowsCreateWithMissingId: Boolean,
    val docs: String? = null
)

data class DeleteOperationModel(
    val docs: String? = null
)

data class ListViewModel(
    val filters: ObjectInputSchema,
    val paging: PagingListInfo? = null,
    val docs: String? = null
)

