package edu.byu.uapi.server.response

fun <UserContext: Any, Model: Any> uapiResponse(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
: List<ResponseFieldDefinition<UserContext, Model, *, *>> {
    TODO()
}

class UAPIResponseInit<UserContext, Model: Any>() {
    inline fun <reified T> prop(name: String, fn: UAPIPropInit<UserContext, Model, T>.() -> Unit) {

    }
}

class UAPIPropInit<UserContext, Model, Type>() {
    fun getValue(fn: (Model) -> Type?) {

    }

    var key: Boolean? = null

    fun description(fn: Describer<Model, Type>) {

    }

    fun longDescription(fn: Describer<Model, Type>) {

    }

    fun modifiable(fn: (UserContext, Model) -> Boolean) {

    }

    var isSystem: Boolean? = null
    var isDerived: Boolean? = null
    var doc: String? = null
    var displayLabel: String? = null
}
