package edu.byu.uapidsl

import edu.byu.jwt.ByuJwt
import edu.byu.uapidsl.dsl.ResourceInit
import edu.byu.uapidsl.dsl.setOnce
import edu.byu.uapidsl.model.ResourceModel
import java.lang.annotation.Inherited

inline fun <AuthContext : Any> apiModel(block: ApiModelInit<AuthContext>.() -> Unit): UApiModel<AuthContext> {
    val init = ApiModelInit<AuthContext>()
    init.block()
    return init.toModel()
}

class ApiModelInit<AuthContext: Any> : DSLInit(ValidationContext()) {

    private var apiInfoInit: ApiInfoInit by setOnce()

    fun info(block: ApiInfoInit.() -> Unit) {
        apiInfoInit = ApiInfoInit(validation)
        apiInfoInit.block()
    }

    var authContextCreator: AuthContextCreator<AuthContext> by setOnce()

    fun authContext(block: AuthContextCreator<AuthContext>) {
        this.authContextCreator = block
    }

    var resources: MutableList<ResourceModel<AuthContext, *, *>> = mutableListOf()

    inline fun <reified IdType : Any, reified ResourceType : Any> resource(name: String, init: ResourceInit<AuthContext, IdType, ResourceType>.() -> Unit) {
        println("Resource: $name ${ResourceType::class.simpleName}")
        val res = ResourceInit<AuthContext, IdType, ResourceType>(validation, name, IdType::class, ResourceType::class)
        res.init()

        resources.add(res.toResourceModel())
    }

    inline fun extend(block: ExtendInit.() -> Unit) {

    }

    fun toModel(): UApiModel<AuthContext> {
        return UApiModel(
            info = apiInfoInit.toModel(),
            authContextCreator = authContextCreator,
            resources = resources
        )
    }

}

class ApiInfoInit(
    validation: ValidationContext
): DSLInit(validation) {
    var name: String by setOnce()
    var version: String by setOnce()
    var description: String? by setOnce()

    internal fun toModel(): ApiInfo {
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
abstract class DSLInit(protected val validation: ValidationContext) {
}

