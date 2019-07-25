package edu.byu.uapi.server.style.receiverfn.requests

interface UAPIRequest<out Caller, out Parent> {

    val caller: Caller
    val parent: Parent

    interface WithModel<Model> {
        val model: Model
    }

    interface WithInput<Input> {
        val input: Input
    }

    interface WithId<Id> {
        val id: Id
    }

}

interface ReadRequest<Caller, out Parent> : UAPIRequest<Caller, Parent> {

    interface Load<Caller, out Parent> :
        ReadRequest<Caller, Parent>

    interface CanView<Caller, out Parent, Model> :
        ReadRequest<Caller, Parent>,
        UAPIRequest.WithModel<Model>

}

interface ListRequest<Caller, out Parent, out Params> :
    ReadRequest<Caller, Parent> {
    val params: Params

}

interface MutationRequest<Caller, out Parent> : UAPIRequest<Caller, Parent> {
    interface Execute<Caller, out Parent> :
        MutationRequest<Caller, Parent>

    interface Authorize<Caller, out Parent> :
        MutationRequest<Caller, Parent>

    interface InputMutation<Caller, out Parent, out Input> : MutationRequest<Caller, Parent> {
        interface Execute<Caller, out Parent, Input> :
            InputMutation<Caller, Parent, Input>,
            MutationRequest.Execute<Caller, Parent>,
            UAPIRequest.WithInput<Input>

        interface Authorize<Caller, out Parent> :
            InputMutation<Caller, Parent, Nothing>,
            MutationRequest.Authorize<Caller, Parent>
    }

    interface ModelMutation<Caller, out Parent, Model> : MutationRequest<Caller, Parent>,
                                                         UAPIRequest.WithModel<Model> {
        interface Execute<Caller, out Parent, Model> :
            ModelMutation<Caller, Parent, Model>,
            MutationRequest.Execute<Caller, Parent>

        interface Authorize<Caller, out Parent, Model> :
            ModelMutation<Caller, Parent, Model>,
            MutationRequest.Authorize<Caller, Parent>
    }

    interface IdMutation<Caller, out Parent, Id> : MutationRequest<Caller, Parent>,
                                                   UAPIRequest.WithId<Id> {
        interface Execute<Caller, out Parent, Id> :
            IdMutation<Caller, Parent, Id>,
            MutationRequest.Execute<Caller, Parent>

        interface Authorize<Caller, out Parent, Id> :
            IdMutation<Caller, Parent, Id>,
            MutationRequest.Authorize<Caller, Parent>
    }

    interface Create<Caller, out Parent, out Input> : InputMutation<Caller, Parent, Input> {
        interface Execute<Caller, out Parent, Input> :
            Create<Caller, Parent, Input>,
            InputMutation.Execute<Caller, Parent, Input>

        interface Authorize<Caller, out Parent> :
            Create<Caller, Parent, Nothing>,
            InputMutation.Authorize<Caller, Parent>

    }

    interface Update<Caller, out Parent, Model, out Input> :
        InputMutation<Caller, Parent, Input>,
        ModelMutation<Caller, Parent, Model> {

        interface Execute<Caller, out Parent, Model, Input> :
            Update<Caller, Parent, Model, Input>,
            InputMutation.Execute<Caller, Parent, Input>,
            ModelMutation.Execute<Caller, Parent, Model>

        interface Authorize<Caller, out Parent, Model> :
            Update<Caller, Parent, Model, Nothing>,
            InputMutation.Authorize<Caller, Parent>,
            ModelMutation.Authorize<Caller, Parent, Model>

    }

    interface CreateOrUpdate<Caller, out Parent, out Model, out Input> : InputMutation<Caller, Parent, Input> {

        interface Execute<Caller, out Parent, out Model, Input> :
            CreateOrUpdate<Caller, Parent, Model, Input>,
            InputMutation.Execute<Caller, Parent, Input>

        interface ExecuteCreate<Caller, out Parent, Input> :
            Execute<Caller, Parent, Nothing, Input>,
            Create.Execute<Caller, Parent, Input>

        interface ExecuteUpdate<Caller, out Parent, Model, Input> :
            Execute<Caller, Parent, Model, Input>,
            Update.Execute<Caller, Parent, Model, Input>

        interface Authorize<Caller, out Parent, Model> :
            CreateOrUpdate<Caller, Parent, Model?, Nothing>,
            Create.Authorize<Caller, Parent>,
            Update.Authorize<Caller, Parent, Model?>
    }

    interface Delete<Caller, out Parent, Model> :
        ModelMutation<Caller, Parent, Model>,
        ModelMutation.Execute<Caller, Parent, Model>,
        ModelMutation.Authorize<Caller, Parent, Model>
}
