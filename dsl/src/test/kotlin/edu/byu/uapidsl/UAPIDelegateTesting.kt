package edu.byu.uapidsl

import edu.byu.uapidsl.types.ApiType
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

data class UAPIFieldMeta(
    var apiType: ApiType,
    var description: String? = null
)

class UAPIPropDelegate<Type>(
    val value: Type,
    val meta: UAPIFieldMeta = UAPIFieldMeta(ApiType.MODIFIABLE)
) : ReadOnlyProperty<Any, Type> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Type = value
}

fun <Type> uapiProp(value: Type, apiType: ApiType = ApiType.MODIFIABLE): UAPIPropDelegate<Type> {
    return UAPIPropDelegate(value, UAPIFieldMeta(apiType))
}

class Person(
    personId: String
) {
    val personId: String by uapiProp(personId, ApiType.SYSTEM)
}

val <Type> KProperty0<Type>.meta: UAPIFieldMeta
    get() {
        this.isAccessible = true
        return (this.getDelegate() as UAPIPropDelegate<Type>).meta
    }


fun main(args: Array<String>) {
    val p = Person("id")
    println(p.personId)

    println(p::personId.meta.apiType)
    p::personId.meta.apiType = ApiType.READ_ONLY
    println(p::personId.meta.apiType)

    println(p::personId.meta.description)

    p::personId.meta.description = "descr"

    println(p::personId.meta.description)

}
