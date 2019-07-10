package edu.byu.uapi.server.spi

interface UAPIRequest {
    interface WithCaller<Caller> : UAPIRequest {
        val caller: Caller
    }

    interface WithModel<Model> : UAPIRequest {
        val model: Model
    }

    interface WithInput<Input> : UAPIRequest {
        val input: Input
    }

    interface WithId<Id> : UAPIRequest {
        val id: Id
    }

    interface WithParent<Parent> : UAPIRequest {
        val parent: Parent
    }

    interface Load<Caller>: WithCaller<Caller>
    interface CanViewModel<Caller, Model>: WithCaller<Caller>, WithModel<Model>

    interface Create<Caller, Input>: WithCaller<Caller>, WithInput<Input>
    interface CanUserCreate<Caller, Input>: WithCaller<Caller>, WithInput<Input>

    interface Update<Caller, Model, Input>: WithCaller<Caller>, WithModel<Model>, WithInput<Input>
    interface CanBeUpdated<Model>: WithModel<Model>

    interface Delete<Caller, Model>: WithCaller<Caller>, WithModel<Model>
    interface CanBeDeleted<Model>: WithModel<Model>

    interface List<Caller>: WithCaller<Caller>
}
