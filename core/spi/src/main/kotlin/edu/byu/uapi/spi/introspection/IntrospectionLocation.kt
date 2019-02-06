package edu.byu.uapi.spi.introspection

import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty0

sealed class IntrospectionLocation {

    object Root : IntrospectionLocation() {
        override fun toString(): String {
            return "(root)"
        }
    }
    data class Class(val inClass: KClass<*>) : IntrospectionLocation() {
        override fun toString(): String {
            return inClass.toString()
        }
    }
    data class Property(val inClass: KClass<*>, val prop: KProperty0<*>) : IntrospectionLocation() {
        override fun toString(): String {
            return "$inClass::${prop.name}"
        }
    }
    data class Method(val inClass: KClass<*>, val method: KCallable<*>) : IntrospectionLocation() {
        override fun toString(): String {
            return "$inClass.${method.name}(...)"
        }
    }
    data class Parameter(val inClass: KClass<*>, val method: KCallable<*>, val parameter: KParameter) :
        IntrospectionLocation() {
        override fun toString(): String {
            return "$inClass.${method.name}(... ${parameter.name}, ...)"
        }
    }

    companion object {
        fun of(inClass: KClass<*>) = Class(inClass)
        fun of(inClass: KClass<*>, prop: KProperty0<*>) =
            Property(inClass, prop)

        fun of(inClass: KClass<*>, method: KCallable<*>) =
            Method(inClass, method)

        fun of(inClass: KClass<*>, method: KCallable<*>, parameter: KParameter) =
            Parameter(inClass, method, parameter)
    }
}
