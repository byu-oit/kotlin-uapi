package edu.byu.uapidsl.model

import com.google.common.base.CaseFormat
import edu.byu.uapidsl.dsl.ReadAuthorizer
import edu.byu.uapidsl.dsl.ReadHandler
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

data class ResourceModel<AuthContext : Any, IdType : Any, DomainType : Any>(
  val type: Introspectable<DomainType>,
  val name: String,
  val get: GetOperation<AuthContext, IdType, DomainType>?
)

data class GetOperation<AuthContext : Any, IdType : Any, DomainType : Any>(
  val authorization: ReadAuthorizer<AuthContext, IdType, DomainType>,
  val handle: ReadHandler<AuthContext, IdType, DomainType>
)

interface ListOperation<AuthContext, IdType, DomainType, Filters : Any> {

}

data class Introspectable<Type : Any>(
  val type: KClass<Type>
) {
  val props by lazy(type::memberProperties)

  val uapiPropNames: Collection<String> by lazy {
    this.props.map { it.name }
      .map { CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it) }
  }
}

