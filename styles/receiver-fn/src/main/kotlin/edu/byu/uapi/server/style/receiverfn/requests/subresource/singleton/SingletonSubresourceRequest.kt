package edu.byu.uapi.server.style.receiverfn.requests.subresource.singleton

import edu.byu.uapi.server.style.receiverfn.requests.SingletonEndpointRequest
import edu.byu.uapi.server.style.receiverfn.requests.subresource.SubresourceRequest

sealed class SingletonSubresourceRequest<Caller, Parent> :
    SingletonEndpointRequest<Caller, Parent>,
    SubresourceRequest<Caller, Parent>

//-----------------------------------------------------------
// Read Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

abstract class Load<Caller, Parent> :
    SingletonSubresourceRequest<Caller, Parent>(),
    SingletonEndpointRequest.Load<Caller, Parent>

abstract class CanView<Caller, Parent, Model> :
    SingletonSubresourceRequest<Caller, Parent>(),
    SingletonEndpointRequest.CanView<Caller, Parent, Model>

//</editor-fold>

//-----------------------------------------------------------
// Update Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

sealed class UpdateRequest<Caller, Parent, Model, out Input> :
    SingletonSubresourceRequest<Caller, Parent>(),
    SingletonEndpointRequest.Update<Caller, Parent, Model, Input>

abstract class ExecuteUpdate<Caller, Parent, Model, Input> :
    UpdateRequest<Caller, Parent, Model, Input>(),
    SingletonEndpointRequest.Update.Execute<Caller, Parent, Model, Input>

abstract class AuthorizeUpdate<Caller, Parent, Model> :
    UpdateRequest<Caller, Parent, Model, Nothing>(),
    SingletonEndpointRequest.Update.Authorize<Caller, Parent, Model>

//</editor-fold>

//-----------------------------------------------------------
// Create or Update Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

sealed class CreateOrUpdate<Caller, Parent, out Model, out Input> :
    SingletonSubresourceRequest<Caller, Parent>(),
    SingletonEndpointRequest.CreateOrUpdate<Caller, Parent, Model, Input> {

    abstract class ExecuteCreate<Caller, Parent, Input> :
        CreateOrUpdate<Caller, Parent, Nothing, Input>(),
        SingletonEndpointRequest.CreateOrUpdate.ExecuteCreate<Caller, Parent, Input>

    abstract class ExecuteUpdate<Caller, Parent, Model, Input> :
        CreateOrUpdate<Caller, Parent, Model, Input>(),
        SingletonEndpointRequest.CreateOrUpdate.ExecuteUpdate<Caller, Parent, Model, Input>

    abstract class Authorize<Caller, Parent, Model> :
        CreateOrUpdate<Caller, Parent, Model?, Nothing>(),
        SingletonEndpointRequest.CreateOrUpdate.Authorize<Caller, Parent, Model?>
}

//</editor-fold>

//-----------------------------------------------------------
// Delete Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

abstract class DeleteRequest<Caller, Parent, Model> :
    SingletonSubresourceRequest<Caller, Parent>(),
    SingletonEndpointRequest.Delete<Caller, Parent, Model>

//<editor-fold defaultstate="collapsed">
