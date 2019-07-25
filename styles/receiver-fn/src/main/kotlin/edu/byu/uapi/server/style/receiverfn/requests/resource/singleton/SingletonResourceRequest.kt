package edu.byu.uapi.server.style.receiverfn.requests.resource.singleton

import edu.byu.uapi.server.style.receiverfn.requests.SingletonEndpointRequest

sealed class SingletonResourceRequest<Caller> : SingletonEndpointRequest<Caller, Nothing> {
    override val parent: Nothing
        get() = TODO("not implemented for top-level resources")
}

//-----------------------------------------------------------
// Read Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

abstract class Load<Caller> :
    SingletonResourceRequest<Caller>(),
    SingletonEndpointRequest.Load<Caller, Nothing>

abstract class CanView<Caller, Model> :
    SingletonResourceRequest<Caller>(),
    SingletonEndpointRequest.CanView<Caller, Nothing, Model>

//</editor-fold>

//-----------------------------------------------------------
// Update Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

sealed class UpdateRequest<Caller, Model, out Input> :
    SingletonResourceRequest<Caller>(),
    SingletonEndpointRequest.Update<Caller, Nothing, Model, Input>

abstract class ExecuteUpdate<Caller, Model, Input> :
    UpdateRequest<Caller, Model, Input>(),
    SingletonEndpointRequest.Update.Execute<Caller, Nothing, Model, Input>

abstract class AuthorizeUpdate<Caller, Model> :
    UpdateRequest<Caller, Model, Nothing>(),
    SingletonEndpointRequest.Update.Authorize<Caller, Nothing, Model>

//</editor-fold>

//-----------------------------------------------------------
// Create Or Update Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">


sealed class CreateOrUpdate<Caller, out Model, out Input> :
    SingletonResourceRequest<Caller>(),
    SingletonEndpointRequest.CreateOrUpdate<Caller, Nothing, Model, Input> {

    abstract class ExecuteCreate<Caller, Input> :
        CreateOrUpdate<Caller, Nothing, Input>(),
        SingletonEndpointRequest.CreateOrUpdate.ExecuteCreate<Caller, Nothing, Input>

    abstract class ExecuteUpdate<Caller, Model, Input> :
        CreateOrUpdate<Caller, Model, Input>(),
        SingletonEndpointRequest.CreateOrUpdate.ExecuteUpdate<Caller, Nothing, Model, Input>

    abstract class Authorize<Caller, Model> :
        CreateOrUpdate<Caller, Model?, Nothing>(),
        SingletonEndpointRequest.CreateOrUpdate.Authorize<Caller, Nothing, Model?>
}

//</editor-fold>

//-----------------------------------------------------------
// Delete Requests
//-----------------------------------------------------------

//<editor-fold defaultstate="collapsed">

abstract class DeleteRequest<Caller, Model> :
    SingletonResourceRequest<Caller>(),
    SingletonEndpointRequest.Delete<Caller, Nothing, Model>

//</editor-fold>
