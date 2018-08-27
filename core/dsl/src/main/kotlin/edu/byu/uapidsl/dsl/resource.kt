package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.DslPart
import edu.byu.uapidsl.ModelingContext
import edu.byu.uapidsl.dsl.subresource.SubresourcesDSL
import edu.byu.uapidsl.model.resource.identified.IdModel
import edu.byu.uapidsl.model.resource.identified.IdentifiedResource
import edu.byu.uapidsl.model.ResponseModel
import edu.byu.uapidsl.model.resource.identified.IdExtractor
import edu.byu.uapidsl.model.resource.identified.IsRestrictedFunc
import edu.byu.uapidsl.types.UAPIField
import kotlin.reflect.KClass

class ResourceDSL<AuthContext: Any, IdType : Any, DomainType : Any>(
    private val name: String,
    private val idType: KClass<IdType>,
    private val modelType: KClass<DomainType>
) : DslPart<IdentifiedResource<AuthContext, IdType, DomainType>>() {

    var example: DomainType by setOnce()

    @PublishedApi
    internal var operationsInit: OperationsDSL<AuthContext, IdType, DomainType> by setOnce()
//    @PublishedApi
//    internal var outputInit: OutputInit<AuthContext, IdType, DomainType, *> by setOnce()
//    private var subresourcesInit: SubresourcesModel<AuthContext, IdType, DomainType> by setOnce()

    inline fun operations(init: OperationsDSL<AuthContext, IdType, DomainType>.() -> Unit) {
        val operations = OperationsDSL<AuthContext, IdType, DomainType>(this)
        operations.init()
        this.operationsInit = operations
    }

//    inline fun <reified UAPIType: Any> output(init: OutputInit<AuthContext, IdType, DomainType, UAPIType>.() -> Unit) {
//        val model = OutputInit<AuthContext, IdType, DomainType, UAPIType>(validation, Introspectable(UAPIType::class))
//        model.init()
//        this.outputInit = model
//    }


    internal var idFromModel: IdExtractor<IdType, DomainType> by setOnce()

    fun idFromModelField(func: IdExtractor<UAPIField<IdType>, DomainType>) {
        idFromModel = {func(it).value}
    }

    fun idFromModel(func: IdExtractor<IdType, DomainType>) {
        idFromModel = func
    }

    private var isRestricted: IsRestrictedFunc<AuthContext, IdType, DomainType>? by setOnce()

    fun isRestricted(func: IsRestrictedFunc<AuthContext, IdType, DomainType>) {
        isRestricted = func
    }

    inline fun subresources(init: SubresourcesDSL<AuthContext, IdType, DomainType>.() -> Unit) {
        //TODO
    }

    override fun toModel(context: ModelingContext): IdentifiedResource<AuthContext, IdType, DomainType> {
        return IdentifiedResource(
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

