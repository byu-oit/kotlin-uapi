package edu.byu.uapi.server.serialization

import java.util.*

class MapTreeSerializationStrategy : TreeSerializationStrategyBase<MapTreeSerializationStrategy,
    SimpleValueSerializationStrategy,
    ListArraySerializationStrategy>() {

    val map: MutableMap<String, Any?> = LinkedHashMap()

    override fun string(
        key: String,
        value: String?
    ) {
        map[key] = value
    }

    override fun number(
        key: String,
        value: Number?
    ) {
        map[key] = value
    }

    override fun boolean(
        key: String,
        value: Boolean?
    ) {
        map[key] = value
    }

    override fun enum(
        key: String,
        value: Enum<*>?
    ) {
        map[key] = value
    }

    override fun treeSerializer(): MapTreeSerializationStrategy = MapTreeSerializationStrategy()

    override fun valueSerializer(): SimpleValueSerializationStrategy = SimpleValueSerializationStrategy()

    override fun arraySerializer(): ListArraySerializationStrategy = ListArraySerializationStrategy()

    override fun addValueFromStrategy(
        key: String,
        strategy: SimpleValueSerializationStrategy?
    ) {
        map[key] = strategy?.value
    }

    override fun addValueFromStrategy(
        key: String,
        strategy: MapTreeSerializationStrategy?
    ) {
        map[key] = strategy?.map
    }

    override fun addValuesFromStrategy(
        key: String,
        strategy: ListArraySerializationStrategy
    ) {
        map[key] = strategy?.list
    }
}

class ListArraySerializationStrategy : ArraySerializationStrategyBase<ListArraySerializationStrategy,
    MapTreeSerializationStrategy,
    SimpleValueSerializationStrategy
    >() {
    val list: MutableList<Any?> = mutableListOf()

    override fun addString(value: String?) {
        list.add(value)
    }

    override fun addNumber(value: Number?) {
        list.add(value)
    }

    override fun addBoolean(value: Boolean?) {
        list.add(value)
    }

    override fun addEnum(value: Enum<*>?) {
        list.add(value)
    }

    override fun treeSerializer(): MapTreeSerializationStrategy = MapTreeSerializationStrategy()

    override fun valueSerializer(): SimpleValueSerializationStrategy = SimpleValueSerializationStrategy()

    override fun arraySerializer(): ListArraySerializationStrategy = ListArraySerializationStrategy()

    override fun addValueFromStrategy(value: MapTreeSerializationStrategy?) {
        list.add(value?.map)
    }

    override fun addValueFromStrategy(value: SimpleValueSerializationStrategy?) {
        list.add(value?.value)
    }

    override fun addValuesFromStrategy(value: ListArraySerializationStrategy?) {
        list.add(value?.list)
    }
}

class SimpleValueSerializationStrategy : ValueSerializationStrategyBase<
    SimpleValueSerializationStrategy,
    MapTreeSerializationStrategy,
    ListArraySerializationStrategy>() {
    var value: Any? = null

    override fun string(value: String?) {
        this.value = value
    }

    override fun number(value: Number?) {
        this.value = value
    }

    override fun boolean(value: Boolean?) {
        this.value = value
    }

    override fun enum(value: Enum<*>?) {
        this.value = value
    }

    override fun value(value: UAPISerializableValue?) {
        if (value == null) {
            this.value = null
        } else {
            value.serialize(this)
        }
    }

    override fun treeSerializer(): MapTreeSerializationStrategy = MapTreeSerializationStrategy()

    override fun arraySerializer(): ListArraySerializationStrategy = ListArraySerializationStrategy()

    override fun addValueFromStrategy(strategy: MapTreeSerializationStrategy?) {
        this.value = strategy?.map
    }

    override fun addValuesFromStrategy(strategy: ListArraySerializationStrategy) {
        this.value = strategy.list
    }
}
