package edu.byu.uapi.server.types

sealed class ModelHolder

abstract class SingletonModel<Model : Any> : ModelHolder() {
    abstract val model: Model
    data class Simple<Model : Any>(override val model: Model) : SingletonModel<Model>()
    class Lazy<Model : Any>(
        getModel: () -> Model
    ) : SingletonModel<Model>() {
        override val model by lazy(getModel)
    }
}

abstract class IdentifiedModel<Id : Any, Model : Any> : ModelHolder() {
    abstract val id: Id
    abstract val model: Model

    data class Simple<Id : Any, Model : Any>(
        override val id: Id,
        override val model: Model
    ) : IdentifiedModel<Id, Model>()

    class Lazy<Id : Any, Model : Any>(
        override val id: Id,
        getModel: () -> Model
    ) : IdentifiedModel<Id, Model>() {
        override val model by lazy(getModel)
    }
}
