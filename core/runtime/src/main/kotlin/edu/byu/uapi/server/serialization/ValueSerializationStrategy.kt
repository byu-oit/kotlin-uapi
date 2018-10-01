package edu.byu.uapi.server.serialization

interface ValueSerializationStrategy: SerializationStrategy {

    fun string(value: String?)
    fun strings(values: Collection<String>)
    fun strings(values: Array<String>)

    fun number(value: Number?)
    fun numbers(values: Collection<Number>)
    fun numbers(values: IntArray)
    fun numbers(values: LongArray)
    fun numbers(values: FloatArray)
    fun numbers(values: DoubleArray)

    fun boolean(value: Boolean?)
    fun booleans(values: Collection<Boolean>)
    fun booleans(values: BooleanArray)

    fun enum(value: Enum<*>?)
    fun enums(values: Collection<Enum<*>?>)
    fun enums(values: Array<Enum<*>?>)

    fun value(value: UAPISerializableValue?)
    fun value(byStrategy: ValueSerializationStrategy.() -> Unit)
    fun values(values: Collection<UAPISerializableValue?>)
    fun values(values: Array<UAPISerializableValue?>)
    fun values(byStrategy: ArraySerializationStrategy.() -> Unit)

    fun tree(value: UAPISerializableTree?)
    fun tree(byStrategy: TreeSerializationStrategy.() -> Unit)
    fun tree(value: Map<String, UAPISerializable<*>?>)
    fun trees(values: Collection<UAPISerializableTree?>)
    fun trees(values: Array<UAPISerializableTree?>)

    fun serializable(value: UAPISerializable<*>?)

}

abstract class ValueSerializationStrategyBase<
    Self: ValueSerializationStrategyBase<Self, TreeStrat, ArrayStrat>,
    TreeStrat: TreeSerializationStrategy,
    ArrayStrat: ArraySerializationStrategy>: ValueSerializationStrategy {

    protected abstract fun treeSerializer(): TreeStrat
    protected abstract fun arraySerializer(): ArrayStrat


    protected abstract fun addValueFromStrategy(
        strategy: TreeStrat?
    )

    protected abstract fun addValuesFromStrategy(
        strategy: ArrayStrat
    )


    override fun strings(values: Collection<String>) {
        val strat = arraySerializer()
        values.forEach(strat::addString)
        addValuesFromStrategy(strat)
    }

    override fun strings(values: Array<String>) {
        val strat = arraySerializer()
        values.forEach(strat::addString)
        addValuesFromStrategy(strat)
    }

    override fun numbers(values: Collection<Number>) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        addValuesFromStrategy(strat)
    }

    override fun numbers(values: IntArray) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        addValuesFromStrategy(strat)
    }

    override fun numbers(values: LongArray) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        addValuesFromStrategy(strat)
    }

    override fun numbers(values: FloatArray) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        addValuesFromStrategy(strat)
    }

    override fun numbers(values: DoubleArray) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        addValuesFromStrategy(strat)
    }

    override fun booleans(values: Collection<Boolean>) {
        val strat = arraySerializer()
        values.forEach(strat::addBoolean)
        addValuesFromStrategy(strat)
    }

    override fun booleans(values: BooleanArray) {
        val strat = arraySerializer()
        values.forEach(strat::addBoolean)
        addValuesFromStrategy(strat)
    }

    override fun enums(values: Collection<Enum<*>?>) {
        val strat = arraySerializer()
        values.forEach(strat::addEnum)
        addValuesFromStrategy(strat)
    }

    override fun enums(values: Array<Enum<*>?>) {
        val strat = arraySerializer()
        values.forEach(strat::addEnum)
        addValuesFromStrategy(strat)
    }

    override fun value(byStrategy: ValueSerializationStrategy.() -> Unit) {
        this.byStrategy()
    }

    override fun values(values: Collection<UAPISerializableValue?>) {
        val strat = arraySerializer()
        values.forEach(strat::addValue)
        addValuesFromStrategy(strat)
    }

    override fun values(values: Array<UAPISerializableValue?>) {
        val strat = arraySerializer()
        values.forEach(strat::addValue)
        addValuesFromStrategy(strat)
    }

    override fun values(byStrategy: ArraySerializationStrategy.() -> Unit) {
        val strat = arraySerializer()
        strat.byStrategy()
        addValuesFromStrategy(strat)
    }

    override fun tree(value: UAPISerializableTree?) {
        val strat= if (value == null) {
            null
        } else {
            val strat = treeSerializer()
            value.serialize(strat)
            strat
        }
        this.addValueFromStrategy(strat)
    }

    override fun tree(byStrategy: TreeSerializationStrategy.() -> Unit) {
        val strat = treeSerializer()
        strat.byStrategy()
        addValueFromStrategy(strat)
    }

    override fun tree(value: Map<String, UAPISerializable<*>?>) {
        val strat = treeSerializer()
        value.forEach { k, v -> strat.serializable(k, v) }
        this.addValueFromStrategy(strat)
    }

    override fun trees(values: Collection<UAPISerializableTree?>) {
        val strat = arraySerializer()
        values.forEach(strat::addTree)
        this.addValuesFromStrategy(strat)
    }

    override fun trees(values: Array<UAPISerializableTree?>) {
        val strat = arraySerializer()
        values.forEach(strat::addTree)
        this.addValuesFromStrategy(strat)
    }

    override fun serializable(value: UAPISerializable<*>?) {
        when (value) {
            is UAPISerializableTree -> this.tree(value)
            is UAPISerializableValue -> this.value(value)
            null -> this.value(null)
        }
    }
}

abstract class NullsAreSpecialValueStrategyBase<
    Self: NullsAreSpecialValueStrategyBase<Self, TreeStrat, ArrayStrat>,
    TreeStrat: TreeSerializationStrategy,
    ArrayStrat: ArraySerializationStrategy>: ValueSerializationStrategyBase<Self, TreeStrat, ArrayStrat>() {

    protected abstract fun nullValue()

    override fun string(value: String?) {
        if (value == null) {
            this.nullValue()
        } else {
            this.safeString(value)
        }
    }

    protected abstract fun safeString(value: String)

    override fun number(value: Number?) {
        if (value == null) {
            this.nullValue()
        } else {
            this.safeNumber(value)
        }
    }

    protected abstract fun safeNumber(value: Number)

    override fun boolean(value: Boolean?) {
        if (value == null) {
            this.nullValue()
        } else {
            this.safeBoolean(value)
        }
    }

    protected abstract fun safeBoolean(value: Boolean)

    override fun enum(value: Enum<*>?) {
        if (value == null) {
            this.nullValue()
        } else {
            this.safeEnum(value)
        }
    }

    protected abstract fun safeEnum(value: Enum<*>)

    override fun value(value: UAPISerializableValue?) {
        if (value == null) {
            this.nullValue()
        } else {
            value.serialize(this)
        }
    }

    override fun addValueFromStrategy(strategy: TreeStrat?) {
        if (strategy == null) {
            this.nullValue()
        } else {
            this.safeAddValueFromStrategy(strategy)
        }
    }

    protected abstract fun safeAddValueFromStrategy(strategy: TreeStrat)
}

