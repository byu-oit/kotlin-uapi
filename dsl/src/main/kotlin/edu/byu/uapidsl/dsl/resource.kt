package edu.byu.uapidsl.dsl

import edu.byu.uapidsl.DSLInit
import edu.byu.uapidsl.ValidationContext
import edu.byu.uapidsl.model.Introspectable
import edu.byu.uapidsl.model.ResourceModel
import kotlin.reflect.KClass

class ResourceInit<AuthContext, IdType : Any, DomainType : Any> (
  validation: ValidationContext,
  private val name: String,
  private val idType: KClass<IdType>,
  private val modelType: KClass<DomainType>
): DSLInit(validation) {

  private var operationsInit: OperationsInit<AuthContext, IdType, DomainType> by setOnce()
  private var modelInit: ModelInit<AuthContext, IdType, DomainType> by setOnce()

  fun operations(init: OperationsInit<AuthContext, IdType, DomainType>.() -> Unit) {
    val operations = OperationsInit<AuthContext, IdType, DomainType>(validation)
    operations.init()
    this.operationsInit = operations
  }

  fun model(init: ModelInit<AuthContext, IdType, DomainType>.() -> Unit) {
    val model = ModelInit<AuthContext, IdType, DomainType>(validation)
    model.init()
    this.modelInit = model
  }

  fun toResourceModel(): ResourceModel<AuthContext, IdType, DomainType> {
    val ops = this.operationsInit
    val model = this.modelInit
    return ResourceModel(
      type = Introspectable(modelType),
      idType = Introspectable(idType),
      name = name,
      read = ops.readModel,
      list = ops.listModel,
      create = ops.createModel,
      update = ops.updateModel,
      delete = ops.deleteModel,
      example = model.example,
      transform = model.transformModel
    )
  }

}
