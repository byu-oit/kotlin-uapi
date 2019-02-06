package edu.byu.uapi.spi.introspection

interface Introspectable<out R> {
    fun introspect(context: IntrospectionContext): R
}
