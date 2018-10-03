package edu.byu.uapi.server.rendering

interface ScalarRenderer<out ValueType> {
    fun string(value: String): ValueType
    fun number(value: Int): ValueType
    fun number(value: Long): ValueType
    fun number(value: Float): ValueType
    fun number(value: Double): ValueType
    fun number(value: Number): ValueType
    fun boolean(value: Boolean): ValueType
    fun nullValue(): ValueType
}

object SimpleScalarRenderer : ScalarRenderer<Any?> {

    override fun string(value: String) = value

    override fun number(value: Int) = value

    override fun number(value: Long) = value

    override fun number(value: Float) = value

    override fun number(value: Double) = value

    override fun number(value: Number) = value

    override fun boolean(value: Boolean) = value

    override fun nullValue(): Any? = null
}
