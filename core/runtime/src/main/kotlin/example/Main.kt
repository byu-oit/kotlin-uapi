package edu.byu.uapi.server.example

import edu.byu.uapi.server.IdentifiedResource

fun main(args: Array<String>) {
    val resource = ExampleResource()
    val runtime = UAPIRuntime<User>()

    runtime.resource("example", resource)

    runtime.startSpark(port = 8080)
}

class UAPIRuntime<UserContext: Any>() {
    fun <Id: Any, Model: Any>resource(
        name: String,
        resource: IdentifiedResource<UserContext, Id, Model>
    ) {
        TODO("not implemented")
    }
}

fun UAPIRuntime<*>.startSpark(port: Int = 4567) {

}

class CommonDelete<Id: Any, Model: Any>: IdentifiedResource.Deletable<User, Id, Model> {
    override fun canUserDelete(userContext: User, id: Id, model: Model): Boolean {
        TODO("not implemented")
    }

    override fun canBeDeleted(id: Id, model: Model): Boolean {
        TODO("not implemented")
    }

    override fun handleDelete(userContext: User, id: Id, model: Model) {
        TODO("not implemented")
    }

}
