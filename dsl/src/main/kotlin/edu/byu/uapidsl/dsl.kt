package edu.byu.uapidsl

import edu.byu.jwt.ByuJwt
import edu.byu.uapidsl.dsl.ResourceInit

inline fun <AuthContext: Any> apiModel(init: ApiModelInit<AuthContext>.() -> Unit): UApiModel<AuthContext> {
    val model = ApiModelInit<AuthContext>()
    model.init()
    return UApiModel(
      resources = emptyList()
    )
}

@UApiMarker
class ApiModelInit<AuthContext> {

    fun authContext(block: AuthContextCreator<AuthContext>) {
    }

    inline fun <reified IdType, reified ResourceModel> resource(name: String, init: ResourceInit<AuthContext, IdType, ResourceModel>.() -> Unit) {
    }

}

typealias AuthContextCreator<AuthContext> = AuthContextInput.() -> AuthContext

data class AuthContextInput(
        val headers: Map<String, String>,
        val jwt: ByuJwt
)

@DslMarker
annotation class UApiMarker

