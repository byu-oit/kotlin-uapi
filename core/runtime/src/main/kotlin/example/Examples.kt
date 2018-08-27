package edu.byu.uapi.server.example

import edu.byu.uapi.server.CollectionWithTotal
import edu.byu.uapi.server.IdentifiedResource
import edu.byu.uapi.server.PagingParams
import edu.byu.uapi.server.validation.Validating
import kotlin.reflect.KClass

class ExampleResource
    : IdentifiedResource<User, String, ExampleDTO>,
    IdentifiedResource.Creatable<User, String, ExampleDTO, ExampleInput>,
    IdentifiedResource.PagedListable<User, String, ExampleDTO, ExampleFilters>
{
    override val deleteOperation = CommonDelete<String, ExampleDTO>()

    override val idType: KClass<String> = String::class
    override val modelType: KClass<ExampleDTO> = ExampleDTO::class

    override val createInput: KClass<ExampleInput> = ExampleInput::class

    override fun loadModel(userContext: User, id: String): ExampleDTO? {
        TODO("not implemented")
    }

    override fun canUserViewModel(userContext: User, id: String, model: ExampleDTO): Boolean {
        TODO("not implemented")
    }

    override fun idFromModel(model: ExampleDTO): String {
        TODO("not implemented")
    }

    override fun canUserCreate(userContext: User): Boolean {
        TODO("not implemented")
    }

    override fun validateCreateInput(userContext: User, input: ExampleInput, validation: Validating) {
        TODO("not implemented")
    }

    override fun handleCreate(userContext: User, input: ExampleInput): String {
        TODO("not implemented")
    }


    override val filterType: KClass<ExampleFilters> = ExampleFilters::class
    override val defaultPageSize: Int = 100
    override val maxPageSize: Int = 100

    override fun list(userContext: User, filters: ExampleFilters, paging: PagingParams): CollectionWithTotal<ExampleDTO> {
        TODO("not implemented")
    }
}

class User
class ExampleDTO
class ExampleInput
class ExampleFilters
