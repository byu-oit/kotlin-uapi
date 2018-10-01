package edu.byu.uapi.server.serialization

interface ArraySerializationStrategy {

    fun addString(value: String?)
    fun addStrings(values: Collection<String>)
    fun addStrings(values: Array<String>)

    fun addNumber(value: Number?)
    fun addNumbers(values: Collection<Number>)
    fun addNumbers(values: IntArray)
    fun addNumbers(values: LongArray)
    fun addNumbers(values: FloatArray)
    fun addNumbers(values: DoubleArray)

    fun addBoolean(value: Boolean?)
    fun addBooleans(values: Collection<Boolean>)
    fun addBooleans(values: BooleanArray)

    fun addEnum(value: Enum<*>?)
    fun addEnums(values: Collection<Enum<*>?>)
    fun addEnums(values: Array<Enum<*>?>)

    fun addValue(value: UAPISerializableValue?)
    fun addValue(byStrategy: ValueSerializationStrategy.() -> Unit)
    fun addValues(values: Collection<UAPISerializableValue?>)
    fun addValues(values: Array<UAPISerializableValue?>)
    fun addValues(byStrategy: ArraySerializationStrategy.() -> Unit)

    fun addTree(value: UAPISerializableTree?)
    fun addTree(byStrategy: TreeSerializationStrategy.() -> Unit)
    fun addTree(value: Map<String, UAPISerializable<*>>)
    fun addTrees(values: Collection<UAPISerializableTree?>)
    fun addTrees(values: Array<UAPISerializableTree?>)

    fun mergeValues(values: Collection<UAPISerializable<*>>)
    fun mergeValues(values: Array<UAPISerializable<*>>)

    fun addSerializable(value: UAPISerializable<*>?)

}

abstract class ArraySerializationStrategyBase<
    Self : ArraySerializationStrategyBase<Self, TreeStrat, ValueStrat>,
    TreeStrat : TreeSerializationStrategy,
    ValueStrat : ValueSerializationStrategy
    > : ArraySerializationStrategy {

    protected abstract fun treeSerializer(): TreeStrat
    protected abstract fun valueSerializer(): ValueStrat
    protected abstract fun arraySerializer(): Self

    protected abstract fun addValueFromStrategy(value: TreeStrat?)
    protected abstract fun addValueFromStrategy(value: ValueStrat?)
    protected abstract fun addValuesFromStrategy(value: Self?)

    override fun addStrings(values: Collection<String>) {
        val strat = arraySerializer()
        values.forEach(strat::addString)
        this.addValuesFromStrategy(strat)
    }

    override fun addStrings(values: Array<String>) {
        val strat = arraySerializer()
        values.forEach(strat::addString)
        this.addValuesFromStrategy(strat)
    }

    override fun addNumbers(values: Collection<Number>) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        this.addValuesFromStrategy(strat)
    }

    override fun addNumbers(values: IntArray) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        this.addValuesFromStrategy(strat)
    }

    override fun addNumbers(values: LongArray) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        this.addValuesFromStrategy(strat)
    }

    override fun addNumbers(values: FloatArray) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        this.addValuesFromStrategy(strat)
    }

    override fun addNumbers(values: DoubleArray) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        this.addValuesFromStrategy(strat)
    }

    override fun addBooleans(values: Collection<Boolean>) {
        val strat = arraySerializer()
        values.forEach(strat::addBoolean)
        this.addValuesFromStrategy(strat)
    }

    override fun addBooleans(values: BooleanArray) {
        val strat = arraySerializer()
        values.forEach(strat::addBoolean)
        this.addValuesFromStrategy(strat)
    }

    override fun addEnums(values: Collection<Enum<*>?>) {
        val strat = arraySerializer()
        values.forEach(strat::addEnum)
        this.addValuesFromStrategy(strat)
    }

    override fun addEnums(values: Array<Enum<*>?>) {
        val strat = arraySerializer()
        values.forEach(strat::addEnum)
        this.addValuesFromStrategy(strat)
    }

    override fun addValue(value: UAPISerializableValue?) {
        val strat = if (value == null) {
            null
        } else {
            val s = valueSerializer()
            value.serialize(s)
            s
        }
        this.addValueFromStrategy(strat)

    }

    override fun addValue(byStrategy: ValueSerializationStrategy.() -> Unit) {
        val strat = valueSerializer()
        strat.byStrategy()
        this.addValueFromStrategy(strat)
    }

    override fun addValues(values: Collection<UAPISerializableValue?>) {
        val strat = arraySerializer()
        values.forEach(strat::addValue)
        this.addValuesFromStrategy(strat)
    }

    override fun addValues(values: Array<UAPISerializableValue?>) {
        val strat = arraySerializer()
        values.forEach(strat::addValue)
        this.addValuesFromStrategy(strat)
    }

    override fun addValues(byStrategy: ArraySerializationStrategy.() -> Unit) {
        val strat = arraySerializer()
        strat.byStrategy()
        this.addValuesFromStrategy(strat)
    }

    override fun addTree(value: UAPISerializableTree?) {
        val strat = if (value == null) {
            null
        } else {
            val s = treeSerializer()
            value.serialize(s)
            s
        }
        this.addValueFromStrategy(strat)
    }

    override fun addTree(byStrategy: TreeSerializationStrategy.() -> Unit) {
        val strat = treeSerializer()
        strat.byStrategy()
        this.addValueFromStrategy(strat)
    }

    override fun addTree(value: Map<String, UAPISerializable<*>>) {
        val strat = treeSerializer()
        value.forEach(strat::serializable)
        this.addValueFromStrategy(strat)
    }

    override fun addTrees(values: Collection<UAPISerializableTree?>) {
        val strat = arraySerializer()
        values.forEach(strat::addTree)
        this.addValuesFromStrategy(strat)
    }

    override fun addTrees(values: Array<UAPISerializableTree?>) {
        val strat = arraySerializer()
        values.forEach(strat::addTree)
        this.addValuesFromStrategy(strat)
    }

    override fun mergeValues(values: Collection<UAPISerializable<*>>) {
        values.forEach(this::addSerializable)
    }

    override fun mergeValues(values: Array<UAPISerializable<*>>) {
        values.forEach(this::addSerializable)
    }

    override fun addSerializable(value: UAPISerializable<*>?) {
        when(value) {
            is UAPISerializableTree -> this.addTree(value)
            is UAPISerializableValue -> this.addValue(value)
            null -> this.addValue(null)
        }
    }
}

abstract class NullsAreSpecialArrayStrategyBase<
    Self: NullsAreSpecialArrayStrategyBase<Self, TreeStrat, ValueStrat>,
    TreeStrat: TreeSerializationStrategy,
    ValueStrat: ValueSerializationStrategy>: ArraySerializationStrategyBase<Self, TreeStrat, ValueStrat>() {

    protected abstract fun addNull()

    override fun addString(value: String?) {
        if (value == null) {
            this.addNull()
        } else {
            this.safeAddString(value)
        }
    }

    protected abstract fun safeAddString(value: String)

    override fun addNumber(value: Number?) {
        if (value == null) {
            this.addNull()
        } else {
            this.safeAddNumber(value)
        }
    }

    protected abstract fun safeAddNumber(value: Number)

    override fun addBoolean(value: Boolean?) {
        if (value == null) {
            this.addNull()
        } else {
            this.safeAddBoolean(value)
        }
    }

    protected abstract fun safeAddBoolean(value: Boolean)

    override fun addEnum(value: Enum<*>?) {
        if (value == null) {
            this.addNull()
        } else {
            this.safeAddEnum(value)
        }
    }

    protected abstract fun safeAddEnum(value: Enum<*>)

    override fun addValueFromStrategy(value: TreeStrat?) {
        if (value == null) {
            this.addNull()
        } else {
            this.safeAddValueFromStrategy(value)
        }
    }

    protected abstract fun safeAddValueFromStrategy(value: TreeStrat)

    override fun addValueFromStrategy(value: ValueStrat?) {
        if (value == null) {
            this.addNull()
        } else {
            this.safeAddValueFromStrategy(value)
        }
    }

    protected abstract fun safeAddValueFromStrategy(value: ValueStrat)

    override fun addValuesFromStrategy(value: Self?) {
        if (value == null) {
            this.addNull()
        } else {
            this.safeAddValuesFromStrategy(value)
        }
    }

    protected abstract fun safeAddValuesFromStrategy(value: Self)
}
