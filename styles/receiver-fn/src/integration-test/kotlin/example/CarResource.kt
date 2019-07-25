package example

import edu.byu.uapi.server.style.receiverfn.requests.MutationRequest
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.CanView
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.CreateRequest
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.DeleteRequest
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.ExecuteCreate
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.ExecuteUpdate
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.ListRequest
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.Load
import edu.byu.uapi.server.style.receiverfn.requests.resource.list.UpdateRequest
import edu.byu.uapi.server.style.receiverfn.CreatableListResource
import edu.byu.uapi.server.style.receiverfn.CreateResult
import edu.byu.uapi.server.style.receiverfn.DeletableListResource
import edu.byu.uapi.server.style.receiverfn.DeleteResult
import edu.byu.uapi.server.style.receiverfn.ListResource
import edu.byu.uapi.server.style.receiverfn.UpdatableListResource
import edu.byu.uapi.server.style.receiverfn.UpdateResult

class CarResource : ListResource<Caller, Vin, Car, CarParams>,
                    CreatableListResource<Caller, Vin, Car, CreateCar>,
                    UpdatableListResource<Caller, Vin, Car, UpdateCar>,
                    DeletableListResource<Caller, Vin, Car> {

    override suspend fun Load<Caller, Vin>.loadModel(): Car? {
        id
        caller
        TODO("not implemented")
    }

    override suspend fun CanView<Caller, Vin, Car>.canUserView(): Boolean {
        caller.authorized
        TODO("not implemented")
    }

    override val Car.uapiId: Vin
        get() = Vin()

    override suspend fun ListRequest<Caller, CarParams>.list(): List<Car> {
        TODO("not implemented")
    }

    fun <R : MutationRequest<*, *>, E : MutationRequest.Execute<*, *>>
        R.ifActualRequest(func: E.() -> Boolean): Boolean {
        TODO()
    }

    override suspend fun CreateRequest<Caller, CreateCar>.canUserCreate(): Boolean {
        if (!caller.authorized) {
            return false
        }
//        return ifActualRequest<> { !input.isRed }
        TODO()
    }

    override suspend fun ExecuteCreate<Caller, CreateCar>.create(): CreateResult<Car> {
        TODO("not implemented")
    }

    override suspend fun UpdateRequest<Caller, Vin, Car, UpdateCar>.canUserUpdate(): Boolean {
        TODO("not implemented")
    }

    override suspend fun UpdateRequest<Caller, Vin, Car, UpdateCar>.canBeUpdated(): Boolean {
        TODO("not implemented")
    }

    override suspend fun ExecuteUpdate<Caller, Vin, Car, UpdateCar>.update(): UpdateResult<Car> {
        TODO("not implemented")
    }

    override suspend fun DeleteRequest<Caller, Vin, Car>.canUserDelete(): Boolean {
        TODO("not implemented")
    }

    override suspend fun DeleteRequest<Caller, Vin, Car>.canBeDeleted(): Boolean {
        TODO("not implemented")
    }

    override suspend fun DeleteRequest<Caller, Vin, Car>.delete(): DeleteResult {
        TODO("not implemented")
    }

}

class CarParams
class Caller(
    val authorized: Boolean
)

class Car
class Vin

class CreateCar(
    val isRed: Boolean
)

class UpdateCar(
    val isRed: Boolean
)
