package edu.byu.uapidsl

import edu.byu.jwt.ByuJwt
import edu.byu.uapidsl.dsl.ResourceDSL
import edu.byu.uapidsl.dsl.setOnce
import edu.byu.uapidsl.typemodeling.DefaultTypeModeler
import edu.byu.uapidsl.typemodeling.TypeModeler
import java.lang.annotation.Inherited

inline fun <AuthContext : Any> apiModel(block: ApiModelDSL<AuthContext>.() -> Unit): UApiModel<AuthContext> {
    val init = ApiModelDSL<AuthContext>()
    init.block()
    return init.toModel(ModelingContext(
        ValidationContext(),
        DefaultTypeModeler()
    ))
}

class ApiModelDSL<AuthContext: Any> : DslPart<UApiModel<AuthContext>>() {

    private var apiInfoInit: ApiInfoDSL by setOnce()

    fun info(block: ApiInfoDSL.() -> Unit) {
        apiInfoInit = ApiInfoDSL()
        apiInfoInit.block()
    }

    var authContextCreator: AuthContextCreator<AuthContext> by setOnce()

    fun authContext(block: AuthContextCreator<AuthContext>) {
        this.authContextCreator = block
    }

    @PublishedApi
    internal var resources: MutableList<ResourceDSL<AuthContext, *, *>> = mutableListOf()

    inline fun <reified IdType : Any, reified ResourceType : Any> resource(name: String, init: ResourceDSL<AuthContext, IdType, ResourceType>.() -> Unit) {
        println("Resource: $name ${ResourceType::class.simpleName}")
        val res = ResourceDSL<AuthContext, IdType, ResourceType>(name, IdType::class, ResourceType::class)
        res.init()

        resources.add(res)
    }

    inline fun extend(block: ExtendInit.() -> Unit) {

    }

    override fun toModel(context: ModelingContext): UApiModel<AuthContext> {
        return UApiModel(
            info = apiInfoInit.toModel(context),
            authContextCreator = authContextCreator,
            resources = resources.map { it.toModel(context) }
        )
    }

}

class ApiInfoDSL: DslPart<ApiInfo>() {
    var name: String by setOnce()
    var version: String by setOnce()
    var description: String? by setOnce()

    override fun toModel(context: ModelingContext): ApiInfo {
        return ApiInfo(
            name = name,
            description = description,
            version = version
        )
    }
}

class ExtendInit {
//    fun scalar() {
//    }

//    fun module() {
//    }
}

typealias AuthContextCreator<AuthContext> = AuthContextInput.() -> AuthContext

data class AuthContextInput(
    val headers: Map<String, Set<String>>,
    val jwt: ByuJwt,
    val originalJwt: ByuJwt? = null
)

@DslMarker
@Inherited
annotation class UApiMarker

@UApiMarker
abstract class DslPart<out ModelType> {
    abstract fun toModel(context: ModelingContext): ModelType
}

data class ModelingContext(
    val validation: ValidationContext,
    val models: TypeModeler
)

