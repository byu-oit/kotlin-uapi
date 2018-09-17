package edu.byu.uapi.server.types

interface UAPISerializable {
    fun serialize(ser: SerializationStrategy)
}

interface SerializationStrategy {

    fun add(
        key: String,
        v: String?
    )

    fun add(
        key: String,
        v: Number?
    )

    fun add(
        key: String,
        v: Boolean?
    )

    fun add(
        key: String,
        v: Enum<*>?
    )

    fun obj(
        key: String,
        v: UAPISerializable?
    )

    fun obj(
        key: String,
        fn: SerializationStrategy.() -> Unit
    )

    fun obj(
        key: String,
        v: Map<String, UAPISerializable?>
    )

    fun strings(
        key: String,
        v: Collection<String>
    )

    fun strings(
        key: String,
        v: Array<String>
    )

    fun numbers(
        key: String,
        v: Collection<Number>
    )

    fun numbers(
        key: String,
        v: IntArray
    )

    fun numbers(
        key: String,
        v: LongArray
    )

    fun numbers(
        key: String,
        v: FloatArray
    )

    fun numbers(
        key: String,
        v: DoubleArray
    )

    fun booleans(
        key: String,
        v: Collection<Boolean>
    )

    fun booleans(
        key: String,
        v: BooleanArray
    )

    fun objects(
        key: String,
        v: Collection<UAPISerializable>
    )

    fun merge(v: Map<String, UAPISerializable?>)

}

abstract class SerializationStrategyBase<Self : SerializationStrategyBase<Self>> : SerializationStrategy {

    protected abstract fun createSerializer(): Self
    protected abstract fun addFromSerializer(
        key: String,
        ser: Self?
    )

    protected abstract fun addListFromSerializers(
        key: String,
        sers: List<Self>
    )

    private fun UAPISerializable.doSerialize(): Self {
        val s = createSerializer()
        this.serialize(s)
        return s
    }

    override fun obj(
        key: String,
        v: UAPISerializable?
    ) {
        addFromSerializer(key, v?.doSerialize())
    }

    override fun obj(
        key: String,
        fn: SerializationStrategy.() -> Unit
    ) {
        val ser = createSerializer()
        ser.fn()
        addFromSerializer(key, ser)
    }

    override fun obj(
        key: String,
        v: Map<String, UAPISerializable?>
    ) {
        val root = createSerializer()

        v.forEach { k, v2 ->
            root.addFromSerializer(k, v2?.doSerialize())
        }

        addFromSerializer(key, root)
    }

    override fun objects(
        key: String,
        v: Collection<UAPISerializable>
    ) {
        val mapped = v.map { it.doSerialize() }
        addListFromSerializers(key, mapped)
    }

    override fun merge(v: Map<String, UAPISerializable?>) {
        v.forEach {
            obj(it.key, it.value)
        }
    }
}

abstract class NullsAreSpecialSerializationStrategy<Self : NullsAreSpecialSerializationStrategy<Self>> : SerializationStrategyBase<Self>() {

    abstract fun addNull(key: String)

    override fun add(
        key: String,
        v: String?
    ) {
        if (v == null) {
            addNull(key)
        } else {
            safeAdd(key, v)
        }
    }

    protected abstract fun safeAdd(
        key: String,
        v: String
    )

    override fun add(
        key: String,
        v: Number?
    ) {
        if (v == null) {
            addNull(key)
        } else {
            safeAdd(key, v)
        }
    }

    protected abstract fun safeAdd(
        key: String,
        v: Number
    )

    override fun add(
        key: String,
        v: Boolean?
    ) {
        if (v == null) {
            addNull(key)
        } else {
            safeAdd(key, v)
        }
    }

    protected abstract fun safeAdd(
        key: String,
        v: Boolean
    )

    override fun add(
        key: String,
        v: Enum<*>?
    ) {
        if (v == null) {
            addNull(key)
        } else {
            safeAdd(key, v)
        }
    }

    protected abstract fun safeAdd(
        key: String,
        v: Enum<*>
    )

    override fun addFromSerializer(
        key: String,
        ser: Self?
    ) {
        if (ser == null) {
            addNull(key)
        } else {
            safeAddFromSerializer(key, ser)
        }
    }

    abstract fun safeAddFromSerializer(
        key: String,
        ser: Self
    )
}

class MapAndListSerializationStrategy : SerializationStrategyBase<MapAndListSerializationStrategy>() {

    override fun createSerializer(): MapAndListSerializationStrategy = MapAndListSerializationStrategy()

    override fun addFromSerializer(
        key: String,
        ser: MapAndListSerializationStrategy?
    ) {
        map[key] = ser?.map
    }

    override fun addListFromSerializers(
        key: String,
        sers: List<MapAndListSerializationStrategy>
    ) {
        map[key] = sers.map { it.map }
    }

    override fun add(
        key: String,
        v: Enum<*>?
    ) {
        map[key] = v
    }

    override fun add(
        key: String,
        v: String?
    ) {
        map[key] = v
    }

    override fun add(
        key: String,
        v: Number?
    ) {
        map[key] = v
    }

    override fun add(
        key: String,
        v: Boolean?
    ) {
        map[key] = v
    }

    override fun strings(
        key: String,
        v: Collection<String>
    ) {
        map[key] = v.toList()
    }

    override fun strings(
        key: String,
        v: Array<String>
    ) {
        map[key] = v.toList()
    }

    override fun booleans(
        key: String,
        v: Collection<Boolean>
    ) {
        map[key] = v.toList()
    }

    override fun booleans(
        key: String,
        v: BooleanArray
    ) {
        map[key] = v.toList()
    }

    override fun numbers(
        key: String,
        v: Collection<Number>
    ) {
        map[key] = v.toList()
    }

    override fun numbers(
        key: String,
        v: IntArray
    ) {
        map[key] = v.toList()
    }

    override fun numbers(
        key: String,
        v: LongArray
    ) {
        map[key] = v.toList()
    }

    override fun numbers(
        key: String,
        v: FloatArray
    ) {
        map[key] = v.toList()
    }

    override fun numbers(
        key: String,
        v: DoubleArray
    ) {
        map[key] = v.toList()
    }

    val map: MutableMap<String, Any?> = LinkedHashMap()

}

