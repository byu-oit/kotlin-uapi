package edu.byu.uapi.server.style.receiverfn.requests

interface SingletonEndpointRequest<Caller, out Parent> : UAPIRequest<Caller, Parent> {

    interface Load<Caller, out Parent> : SingletonEndpointRequest<Caller, Parent>,
                                         ReadRequest.Load<Caller, Parent>

    interface CanView<Caller, out Parent, Model> : SingletonEndpointRequest<Caller, Parent>,
                                                   ReadRequest.CanView<Caller, Parent, Model>

    interface Update<Caller, out Parent, Model, out Input>
        : ListEndpointRequest<Caller, Parent>,
          MutationRequest.Update<Caller, Parent, Model, Input> {

        interface Execute<Caller, out Parent, Model, Input> :
            Update<Caller, Parent, Model, Input>,
            MutationRequest.Update.Execute<Caller, Parent, Model, Input>

        interface Authorize<Caller, out Parent, Model> :
            Update<Caller, Parent, Model, Nothing>,
            MutationRequest.Update.Authorize<Caller, Parent, Model>

    }

    interface CreateOrUpdate<Caller, out Parent, out Model, out Input> :
        ListEndpointRequest<Caller, Parent>,
        MutationRequest.CreateOrUpdate<Caller, Parent, Model, Input> {

        interface ExecuteCreate<Caller, out Parent, Input> :
            CreateOrUpdate<Caller, Parent, Nothing, Input>,
            MutationRequest.InputMutation.Execute<Caller, Parent, Input>,
            MutationRequest.Create.Execute<Caller, Parent, Input>

        interface ExecuteUpdate<Caller, out Parent, Model, Input> :
            CreateOrUpdate<Caller, Parent, Model, Input>,
            MutationRequest.InputMutation.Execute<Caller, Parent, Input>,
            MutationRequest.Update.Execute<Caller, Parent, Model, Input>

        interface Authorize<Caller, out Parent, Model> :
            CreateOrUpdate<Caller, Parent, Model?, Nothing>,
            MutationRequest.Create.Authorize<Caller, Parent>,
            MutationRequest.Update.Authorize<Caller, Parent, Model?>

    }

    interface Delete<Caller, out Parent, Model> :
        ListEndpointRequest<Caller, Parent>,
        MutationRequest.Delete<Caller, Parent, Model>

}
