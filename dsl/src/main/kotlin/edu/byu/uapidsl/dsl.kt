package edu.byu.uapidsl

import edu.byu.jwt.ByuJwt
import edu.byu.uapidsl.dsl.ResourceInit
import edu.byu.uapidsl.model.ResourceModel

inline fun <AuthContext : Any> apiModel(init: ApiModelInit<AuthContext>.() -> Unit): UApiModel<AuthContext> {
  val model = ApiModelInit<AuthContext>()
  model.init()
  return UApiModel(
    authContextCreator = model.authContextCreator!!,
    resources = model.resources
  )
}

@UApiMarker
class ApiModelInit<AuthContext> {

  var authContextCreator: AuthContextCreator<AuthContext>? = null

  fun authContext(block: AuthContextCreator<AuthContext>) {
    this.authContextCreator = block
  }

  var resources: MutableList<ResourceModel<AuthContext, *, *>> = mutableListOf()

  inline fun <reified IdType: Any, reified ResourceType: Any> resource(name: String, init: ResourceInit<AuthContext, IdType, ResourceType>.() -> Unit) {
    println("Resource: $name ${ResourceType::class.simpleName}")
    val res = ResourceInit<AuthContext, IdType, ResourceType>(name, IdType::class, ResourceType::class)
    res.init()

    resources.add(res.toResourceModel())
  }

}

typealias AuthContextCreator<AuthContext> = AuthContextInput.() -> AuthContext

data class AuthContextInput(
  val headers: Map<String, String>,
  val jwt: ByuJwt
)

@DslMarker
annotation class UApiMarker

