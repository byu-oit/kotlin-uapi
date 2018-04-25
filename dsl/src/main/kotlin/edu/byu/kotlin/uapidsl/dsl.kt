package edu.byu.kotlin.uapidsl

import edu.byu.jwt.ByuJwt
import kotlin.reflect.KClass

inline fun <AuthContext> apiModel(init: ApiModelInit<AuthContext>.() -> Unit): ApiModelInit<AuthContext> {
    val model = ApiModelInit<AuthContext>()
    model.init()
    return model
}

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

