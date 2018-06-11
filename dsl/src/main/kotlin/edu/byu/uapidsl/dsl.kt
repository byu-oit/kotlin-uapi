package edu.byu.uapidsl

import edu.byu.jwt.ByuJwt
import edu.byu.uapidsl.dsl.ResourceInit
import edu.byu.uapidsl.dsl.setOnce
import edu.byu.uapidsl.typemodeling.DefaultTypeModeler
import edu.byu.uapidsl.typemodeling.TypeModeler
import java.lang.annotation.Inherited

inline fun <AuthContext : Any> apiModel(block: ApiModelInit<AuthContext>.() -> Unit): UApiModel<AuthContext> {
    val init = ApiModelInit<AuthContext>()
    init.block()
    return init.toModel(ModelingContext(
        ValidationContext(),
        DefaultTypeModeler()
    ))
}

class ApiModelInit<AuthContext: Any> : DSLInit<UApiModel<AuthContext>>() {

    private var apiInfoInit: ApiInfoInit by setOnce()

    fun info(block: ApiInfoInit.() -> Unit) {
        apiInfoInit = ApiInfoInit()
        apiInfoInit.block()
    }

    var authContextCreator: AuthContextCreator<AuthContext> by setOnce()

    fun authContext(block: AuthContextCreator<AuthContext>) {
        this.authContextCreator = block
    }

    @PublishedApi
    internal var resources: MutableList<ResourceInit<AuthContext, *, *>> = mutableListOf()

    inline fun <reified IdType : Any, reified ResourceType : Any> resource(name: String, init: ResourceInit<AuthContext, IdType, ResourceType>.() -> Unit) {
        println("Resource: $name ${ResourceType::class.simpleName}")
        val res = ResourceInit<AuthContext, IdType, ResourceType>(name, IdType::class, ResourceType::class)
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

class ApiInfoInit: DSLInit<ApiInfo>() {
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
abstract class DSLInit<out ModelType> {
    abstract fun toModel(context: ModelingContext): ModelType
}

data class ModelingContext(
    val validation: ValidationContext,
    val models: TypeModeler
)

