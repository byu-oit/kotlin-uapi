package edu.byu.uapidsl

import edu.byu.jwt.ByuJwt
import edu.byu.uapidsl.dsl.ResourceInit
import kotlin.reflect.KClass

inline fun <AuthContext> apiModel(init: ApiModelInit<AuthContext>.() -> Unit): UApiModel {
    val model = ApiModelInit<AuthContext>()
    model.init()
    return UApiModel()
}


@UApiMarker
class ApiModelInit<AuthContext> {
    private var authContextCreator: AuthContextCreator<AuthContext>? = null
    fun authContext(creator: AuthContextCreator<AuthContext>) {
        this.authContextCreator = creator
    }

    val resources: MutableMap<KClass<*>, ResourceInit<AuthContext, *, *>> = mutableMapOf()

    inline fun <reified IdType, reified ResourceModel> resource(name: String, init: ResourceInit<AuthContext, IdType, ResourceModel>.() -> Unit) {
        val res = ResourceInit<AuthContext, IdType, ResourceModel>(name)
        res.init()
        this.resources[ResourceModel::class] = res
    }


}

typealias AuthContextCreator<AuthContext> = (AuthContextInput) -> AuthContext

data class AuthContextInput(
        val headers: Map<String, String>,
        val jwt: ByuJwt
)

@DslMarker
annotation class UApiMarker

