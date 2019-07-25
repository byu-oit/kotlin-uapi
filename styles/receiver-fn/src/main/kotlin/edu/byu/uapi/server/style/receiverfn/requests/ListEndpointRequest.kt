package edu.byu.uapi.server.style.receiverfn.requests

interface ListEndpointRequest<Caller, out Parent> : UAPIRequest<Caller, Parent> {

    interface Load<Caller, out Parent, Id> : ListEndpointRequest<Caller, Parent>,
                                             ReadRequest.Load<Caller, Parent>, UAPIRequest.WithId<Id>

    interface CanView<Caller, out Parent, Id, Model> : ListEndpointRequest<Caller, Parent>,
                                                       ReadRequest.CanView<Caller, Parent, Model>,
                                                       UAPIRequest.WithId<Id>

    interface List<Caller, out Parent, Params> : ListRequest<Caller, Parent, Params>

    interface Create<Caller, out Parent, out Input> :
        ListEndpointRequest<Caller, Parent>,
        MutationRequest.Create<Caller, Parent, Input> {

        interface Execute<Caller, out Parent, Input> :
            Create<Caller, Parent, Input>,
            MutationRequest.Create.Execute<Caller, Parent, Input>

        interface Authorize<Caller, out Parent> :
            Create<Caller, Parent, Nothing>,
            MutationRequest.Create.Authorize<Caller, Parent>

    }

    interface Update<Caller, out Parent, Id, Model, out Input> :
        ListEndpointRequest<Caller, Parent>,
        MutationRequest.IdMutation<Caller, Parent, Id>,
        MutationRequest.Update<Caller, Parent, Model, Input> {

        interface Execute<Caller, out Parent, Id, Model, Input> :
            Update<Caller, Parent, Id, Model, Input>,
            MutationRequest.IdMutation.Execute<Caller, Parent, Id>,
            MutationRequest.Update.Execute<Caller, Parent, Model, Input>

        interface Authorize<Caller, out Parent, Id, Model> :
            Update<Caller, Parent, Id, Model, Nothing>,
            MutationRequest.IdMutation.Authorize<Caller, Parent, Id>,
            MutationRequest.Update.Authorize<Caller, Parent, Model>
    }

    interface CreateOrUpdate<Caller, out Parent, Id, out Model, out Input> :
        ListEndpointRequest<Caller, Parent>,
        MutationRequest.IdMutation<Caller, Parent, Id>,
        MutationRequest.CreateOrUpdate<Caller, Parent, Model, Input> {

        interface ExecuteCreate<Caller, out Parent, Id, Input> :
            CreateOrUpdate<Caller, Parent, Id, Nothing, Input>,
            MutationRequest.IdMutation.Execute<Caller, Parent, Id>,
            MutationRequest.InputMutation.Execute<Caller, Parent, Input>,
            MutationRequest.Create.Execute<Caller, Parent, Input>

        interface ExecuteUpdate<Caller, out Parent, Id, Model, Input> :
            CreateOrUpdate<Caller, Parent, Id, Model, Input>,
            MutationRequest.IdMutation.Execute<Caller, Parent, Id>,
            MutationRequest.InputMutation.Execute<Caller, Parent, Input>,
            MutationRequest.Update.Execute<Caller, Parent, Model, Input>

        interface Authorize<Caller, out Parent, Id, Model> :
            CreateOrUpdate<Caller, Parent, Id, Model?, Nothing>,
            MutationRequest.IdMutation.Authorize<Caller, Parent, Id>,
            MutationRequest.Create.Authorize<Caller, Parent>,
            MutationRequest.Update.Authorize<Caller, Parent, Model?>

    }

    interface Delete<Caller, out Parent, Id, Model> :
        ListEndpointRequest<Caller, Parent>,
        MutationRequest.IdMutation<Caller, Parent, Id>,
        MutationRequest.Delete<Caller, Parent, Model>

}
