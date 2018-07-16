package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.DSLInit
import edu.byu.uapidsl.ModelingContext
import edu.byu.uapidsl.dsl.subresource.SubresourcesInit
import edu.byu.uapidsl.model.resource.IdModel
import edu.byu.uapidsl.model.resource.ResourceModel
import edu.byu.uapidsl.model.ResponseModel
import kotlin.reflect.KClass

class ResourceInit<AuthContext: Any, IdType : Any, DomainType : Any>(
    private val name: String,
    private val idType: KClass<IdType>,
    private val modelType: KClass<DomainType>
) : DSLInit<ResourceModel<AuthContext, IdType, DomainType>>() {

    var example: DomainType by setOnce()

    @PublishedApi
    internal var operationsInit: OperationsInit<AuthContext, IdType, DomainType> by setOnce()
//    @PublishedApi
//    internal var outputInit: OutputInit<AuthContext, IdType, DomainType, *> by setOnce()
//    private var subresourcesInit: SubresourcesModel<AuthContext, IdType, DomainType> by setOnce()

    inline fun operations(init: OperationsInit<AuthContext, IdType, DomainType>.() -> Unit) {
        val operations = OperationsInit<AuthContext, IdType, DomainType>()
        operations.init()
        this.operationsInit = operations
    }

//    inline fun <reified UAPIType: Any> output(init: OutputInit<AuthContext, IdType, DomainType, UAPIType>.() -> Unit) {
//        val model = OutputInit<AuthContext, IdType, DomainType, UAPIType>(validation, Introspectable(UAPIType::class))
//        model.init()
//        this.outputInit = model
//    }


    private var idFromModel: IdExtractor<IdType, DomainType> by setOnce()

    fun idFromModel(func: IdExtractor<IdType, DomainType>) {
        idFromModel = func
    }

    private var isRestricted: IsRestrictedFunc<AuthContext, IdType, DomainType>? by setOnce()

    fun isRestricted(func: IsRestrictedFunc<AuthContext, IdType, DomainType>) {
        isRestricted = func
    }

    inline fun subresources(init: SubresourcesInit<AuthContext, IdType, DomainType>.() -> Unit) {
        //TODO
    }

    override fun toModel(context: ModelingContext): ResourceModel<AuthContext, IdType, DomainType> {
        return ResourceModel(
            type = modelType,
            responseModel = ResponseModel(
                context.models.outputSchemaFor(modelType),
                context.models.genericJsonWriter()
            ),
            idModel = IdModel(
                context.models.pathParamSchemaFor(idType),
                context.models.pathParamReaderFor(idType)
            ),
            idExtractor = idFromModel,
            name = name,
            example = example,
            operations = this.operationsInit.toModel(context)
        )
    }
}

typealias IdExtractor<IdType, ModelType> = (ModelType) -> IdType
typealias IsRestrictedFunc<AuthContext, IdType, DomainType> = ReadContext<AuthContext, IdType, DomainType>.() -> Boolean
