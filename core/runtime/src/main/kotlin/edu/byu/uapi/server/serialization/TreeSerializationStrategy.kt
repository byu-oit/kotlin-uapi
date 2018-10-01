package edu.byu.uapi.server.serialization

interface TreeSerializationStrategy : SerializationStrategy {

    fun string(
        key: String,
        value: String?
    )

    fun strings(
        key: String,
        values: Collection<String>
    )

    fun strings(
        key: String,
        values: Array<String>
    )

    fun number(
        key: String,
        value: Number?
    )

    fun numbers(
        key: String,
        values: Collection<Number>
    )

    fun numbers(
        key: String,
        values: IntArray
    )

    fun numbers(
        key: String,
        values: LongArray
    )

    fun numbers(
        key: String,
        values: FloatArray
    )

    fun numbers(
        key: String,
        values: DoubleArray
    )

    fun boolean(
        key: String,
        value: Boolean?
    )

    fun booleans(
        key: String,
        values: Collection<Boolean>
    )

    fun booleans(
        key: String,
        values: BooleanArray
    )

    fun enum(
        key: String,
        value: Enum<*>?
    )

    fun enums(
        key: String,
        values: Collection<Enum<*>?>
    )

    fun enums(
        key: String,
        values: Array<Enum<*>?>
    )

    fun value(
        key: String,
        value: UAPISerializableValue?
    )

    fun value(
        key: String,
        byStrategy: ValueSerializationStrategy.() -> Unit
    )

    fun values(
        key: String,
        values: Collection<UAPISerializableValue?>
    )

    fun values(
        key: String,
        values: Array<UAPISerializableValue?>
    )

    fun values(
        key: String,
        byStrategy: ArraySerializationStrategy.() -> Unit
    )

    fun tree(
        key: String,
        value: UAPISerializableTree?
    )

    fun tree(
        key: String,
        byStrategy: TreeSerializationStrategy.() -> Unit
    )

    fun tree(
        key: String,
        value: Map<String, UAPISerializable<*>?>
    )

    fun trees(
        key: String,
        values: Collection<UAPISerializableTree?>
    )

    fun trees(
        key: String,
        values: Array<UAPISerializableTree?>
    )

    fun serializable(
        key: String,
        value: UAPISerializable<*>?
    )

    fun mergeTree(value: Map<String, UAPISerializable<*>?>)
    fun mergeTree(value: UAPISerializableTree?)

}

abstract class TreeSerializationStrategyBase<
    Self : TreeSerializationStrategyBase<Self, ValueStrat, ArrayStrat>,
    ValueStrat : ValueSerializationStrategy,
    ArrayStrat : ArraySerializationStrategy
    > : TreeSerializationStrategy {

    protected abstract fun treeSerializer(): Self
    protected abstract fun valueSerializer(): ValueStrat
    protected abstract fun arraySerializer(): ArrayStrat

    protected abstract fun addValueFromStrategy(
        key: String,
        strategy: ValueStrat?
    )

    protected abstract fun addValueFromStrategy(
        key: String,
        strategy: Self?
    )

    protected abstract fun addValuesFromStrategy(
        key: String,
        strategy: ArrayStrat
    )

    private fun UAPISerializableTree.doSerialize(): Self {
        val s = treeSerializer()
        this.serialize(s)
        return s
    }

    private fun UAPISerializableValue.doSerialize(): ValueStrat {
        val s = valueSerializer()
        this.serialize(s)
        return s
    }

    override fun value(
        key: String,
        value: UAPISerializableValue?
    ) {
        this.addValueFromStrategy(key, value?.doSerialize())
    }

    override fun strings(
        key: String,
        values: Collection<String>
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addString)
        addValuesFromStrategy(key, strat)
    }

    override fun strings(
        key: String,
        values: Array<String>
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addString)
        addValuesFromStrategy(key, strat)
    }

    override fun numbers(
        key: String,
        values: Collection<Number>
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        addValuesFromStrategy(key, strat)
    }

    override fun numbers(
        key: String,
        values: IntArray
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        addValuesFromStrategy(key, strat)
    }

    override fun numbers(
        key: String,
        values: LongArray
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        addValuesFromStrategy(key, strat)
    }

    override fun numbers(
        key: String,
        values: FloatArray
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        addValuesFromStrategy(key, strat)
    }

    override fun numbers(
        key: String,
        values: DoubleArray
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addNumber)
        addValuesFromStrategy(key, strat)
    }

    override fun booleans(
        key: String,
        values: Collection<Boolean>
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addBoolean)
        addValuesFromStrategy(key, strat)
    }

    override fun booleans(
        key: String,
        values: BooleanArray
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addBoolean)
        addValuesFromStrategy(key, strat)
    }

    override fun enums(
        key: String,
        values: Collection<Enum<*>?>
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addEnum)
        addValuesFromStrategy(key, strat)
    }

    override fun enums(
        key: String,
        values: Array<Enum<*>?>
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addEnum)
        addValuesFromStrategy(key, strat)
    }

    override fun value(
        key: String,
        byStrategy: ValueSerializationStrategy.() -> Unit
    ) {
        val strat = valueSerializer()
        strat.byStrategy()
        this.addValueFromStrategy(key, strat)
    }

    override fun values(
        key: String,
        values: Collection<UAPISerializableValue?>
    ) {
        val ser = arraySerializer()
        values.forEach(ser::addValue)
        addValuesFromStrategy(key, ser)
    }

    override fun values(
        key: String,
        values: Array<UAPISerializableValue?>
    ) {
        val strat = arraySerializer()
        values.forEach(strat::addValue)
        addValuesFromStrategy(key, strat)
    }

    override fun values(
        key: String,
        byStrategy: ArraySerializationStrategy.() -> Unit
    ) {
        val strat = arraySerializer()
        strat.byStrategy()
        addValuesFromStrategy(key, strat)
    }

    override fun tree(
        key: String,
        value: UAPISerializableTree?
    ) {
        this.addValueFromStrategy(key, value?.doSerialize())
    }

    override fun tree(
        key: String,
        byStrategy: TreeSerializationStrategy.() -> Unit
    ) {
        val strat = treeSerializer()
        strat.byStrategy()
        this.addValueFromStrategy(key, strat)
    }

    override fun tree(
        key: String,
        value: Map<String, UAPISerializable<*>?>
    ) {
        val rootTree = treeSerializer()
        value.forEach(rootTree::serializable)
        this.addValueFromStrategy(key, rootTree)
    }

    override fun serializable(
        key: String,
        value: UAPISerializable<*>?
    ) {
        when (value) {
            null -> value(key, null)
            is UAPISerializableTree -> tree(key, value)
            is UAPISerializableValue -> value(key, value)
        }
    }

    override fun trees(
        key: String,
        values: Collection<UAPISerializableTree?>
    ) {
        val array = arraySerializer()
        values.forEach(array::addTree)
        this.addValuesFromStrategy(key, array)
    }

    override fun trees(
        key: String,
        values: Array<UAPISerializableTree?>
    ) {
        val array = arraySerializer()
        values.forEach(array::addTree)
        this.addValuesFromStrategy(key, array)
    }

    override fun mergeTree(value: Map<String, UAPISerializable<*>?>) {
        value.forEach(this::serializable)
    }

    override fun mergeTree(value: UAPISerializableTree?) {
        value?.serialize(this)
    }
}

abstract class NullsAreSpecialTreeStrategyBase<
    Self : NullsAreSpecialTreeStrategyBase<Self, ValueStrat, ArrayStrat>,
    ValueStrat : ValueSerializationStrategy,
    ArrayStrat : ArraySerializationStrategy
    > : TreeSerializationStrategyBase<Self, ValueStrat, ArrayStrat>() {

    protected abstract fun nullValue(key: String)

    override fun string(
        key: String,
        value: String?
    ) {
        if (value == null) {
            this.nullValue(key)
        } else {
            this.safeString(key, value)
        }
    }

    protected abstract fun safeString(
        key: String,
        value: String
    )

    override fun number(
        key: String,
        value: Number?
    ) {
        if (value == null) {
            this.nullValue(key)
        } else {
            this.safeNumber(key, value)
        }
    }

    protected abstract fun safeNumber(
        key: String,
        value: Number
    )

    override fun boolean(
        key: String,
        value: Boolean?
    ) {
        if (value == null) {
            this.nullValue(key)
        } else {
            this.safeBoolean(key, value)
        }
    }

    protected abstract fun safeBoolean(
        key: String,
        value: Boolean
    )

    override fun enum(
        key: String,
        value: Enum<*>?
    ) {
        if (value == null) {
            this.nullValue(key)
        } else {
            this.safeEnum(key, value)
        }
    }

    protected abstract fun safeEnum(
        key: String,
        value: Enum<*>
    )

    override fun addValueFromStrategy(
        key: String,
        strategy: ValueStrat?
    ) {
        if (strategy == null) {
            this.nullValue(key)
        } else {
            safeAddValueFromStrategy(key, strategy)
        }
    }

    protected abstract fun safeAddValueFromStrategy(
        key: String,
        strategy: ValueStrat
    )

    override fun addValueFromStrategy(
        key: String,
        strategy: Self?
    ) {
        if (strategy == null) {
            this.nullValue(key)
        } else {
            safeAddValueFromStrategy(key, strategy)
        }
    }

    protected abstract fun safeAddValueFromStrategy(
        key: String,
        strategy: Self
    )
}

