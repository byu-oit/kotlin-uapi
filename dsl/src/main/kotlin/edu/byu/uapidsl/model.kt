package edu.byu.uapidsl

import edu.byu.uapidsl.model.ResourceModel

data class UApiModel<AuthContext: Any>(
  val resources: List<ResourceModel<AuthContext, Any, Any>>
)
