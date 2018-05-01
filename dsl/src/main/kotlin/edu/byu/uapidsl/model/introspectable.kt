package edu.byu.uapidsl.model

import com.google.common.base.CaseFormat
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


data class Introspectable<Type : Any>(
  val type: KClass<Type>
) {
  val props by lazy(type::memberProperties)

  val uapiPropNames: Collection<String> by lazy {
    this.props.map { it.name }
      .map { CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it) }
  }
}

