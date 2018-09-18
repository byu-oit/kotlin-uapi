package edu.byu.uapi.server.response

inline fun <UserContext : Any, Model : Any> uapiResponse(fn: UAPIResponseInit<UserContext, Model>.() -> Unit)
    : List<ResponseFieldDefinition<UserContext, Model, *, *>> {
    val r = UAPIResponseInit<UserContext, Model>()
    r.fn()
    return emptyList()
}

class UAPIResponseInit<UserContext, Model : Any>() {
    inline fun <reified T> prop(
        name: String,
        fn: UAPIPropInit<UserContext, Model, T>.() -> Unit
    ) {
        val p = UAPIPropInit<UserContext, Model, T>(isNullable<T>())
        p.fn()
    }

    inline fun <reified T> isNullable(): Boolean = null is T

}

class UAPIPropInit<UserContext, Model, Type>(
    val nullable: Boolean
) {
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
