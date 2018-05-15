package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.DSLInit
import edu.byu.uapidsl.ValidationContext
import edu.byu.uapidsl.dsl.subresource.SubresourcesInit
import edu.byu.uapidsl.model.*
import edu.byu.uapidsl.types.UAPIField
import kotlin.reflect.KClass

class ResourceInit<AuthContext, IdType : Any, DomainType : Any>(
    validation: ValidationContext,
    private val name: String,
    private val idType: KClass<IdType>,
    private val modelType: KClass<DomainType>
) : DSLInit(validation) {

    var example: DomainType by setOnce()

    private var operationsInit: OperationsInit<AuthContext, IdType, DomainType> by setOnce()
//    @PublishedApi
//    internal var outputInit: OutputInit<AuthContext, IdType, DomainType, *> by setOnce()
//    private var subresourcesInit: SubresourcesModel<AuthContext, IdType, DomainType> by setOnce()

    fun operations(init: OperationsInit<AuthContext, IdType, DomainType>.() -> Unit) {
        val operations = OperationsInit<AuthContext, IdType, DomainType>(validation)
        operations.init()
        this.operationsInit = operations
    }

//    inline fun <reified UAPIType: Any> output(init: OutputInit<AuthContext, IdType, DomainType, UAPIType>.() -> Unit) {
//        val model = OutputInit<AuthContext, IdType, DomainType, UAPIType>(validation, Introspectable(UAPIType::class))
//        model.init()
//        this.outputInit = model
//    }

    inline fun subresources(init: SubresourcesInit<AuthContext, IdType, DomainType>.() -> Unit) {
        //TODO
    }

    fun toResourceModel(): ResourceModel<AuthContext, IdType, DomainType> {
        val ops = this.operationsInit

        val type = Introspectable(modelType)
        val responseModel = getResponseModel(type)

        return ResourceModel(
            type = type,
            responseModel = responseModel,
            idModel = getPathIdentifierModel(this.idType),
            name = name,
            example = example,
            read = ops.readModel,
            list = ops.listModel,
            create = ops.createModel,
            update = ops.updateModel,
            delete = ops.deleteModel,
            // TODO ("Actual response wrapper, maybe? ")
            responseMapper = FakeResponseMapper(responseModel)
        )
    }
}

class FakeResponseMapper<AuthContext, IdType, DomainType: Any>(
    private val responseModel: ResponseModel<DomainType>
): ModelResponseMapper<AuthContext, IdType, DomainType> {
    override fun mapResponse(authContext: AuthContext, idType: IdType, modelType: DomainType): Map<String, UAPIField<*>> {
        return responseModel.fields.map {
            it.uapiName to it.reader(modelType)
        }.toMap()
    }

}
