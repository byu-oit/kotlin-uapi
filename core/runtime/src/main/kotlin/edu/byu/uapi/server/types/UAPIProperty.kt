package edu.byu.uapi.server.types

import edu.byu.uapi.server.scalars.ScalarConverter
import edu.byu.uapi.server.serialization.TreeSerializationStrategy
import edu.byu.uapi.server.serialization.UAPISerializableTree

sealed class UAPIProperty: UAPISerializableTree {
    abstract val apiType: APIType
    abstract val key: Boolean
    abstract val displayLabel: String?
    abstract val domain: OrMissing<String>
    abstract val relatedResource: OrMissing<String>

    final override fun serialize(strategy: TreeSerializationStrategy) {
        serializeValue(strategy)
        strategy.enum("api_type", apiType)
        if (key) {
            strategy.boolean("key", key)
        }
        displayLabel?.let { strategy.string("display_label", it) }
        domain.ifPresent { strategy.string("domain", it) }
        relatedResource.ifPresent { strategy.string("related_resource", it) }
    }

    protected abstract fun serializeValue(ser: TreeSerializationStrategy)
}

sealed class UAPIValueProperty<Value: Any>(
    val value: Value?,
    val scalarConverter: ScalarConverter<Value>,
    val description: OrMissing<String>,
    val longDescription: OrMissing<String>,
    override val apiType: APIType,
    override val key: Boolean,
    override val displayLabel: String?,
    override val domain: OrMissing<String>,
    override val relatedResource: OrMissing<String>
): UAPIProperty() {
    override fun serializeValue(
        ser: TreeSerializationStrategy
    ) {
        ser.value("value") {
            scalarConverter.serialize(value, this)
        }
        description.ifPresent { ser.string("description", it) }
        longDescription.ifPresent { ser.string("long_description", it) }
    }
}

sealed class OrMissing<out Type : Any> {

    abstract fun ifPresent(fn: (Type?) -> Unit)

    data class Present<out Type : Any>(
        val value: Type?
    ) : OrMissing<Type>() {
        override fun ifPresent(fn: (Type?) -> Unit) {
            fn(value)
        }
    }

    object Missing : OrMissing<Nothing>() {
        override fun ifPresent(fn: (Nothing?) -> Unit) {
        }
    }
}

enum class APIType {
    READ_ONLY,
    MODIFIABLE,
    SYSTEM,
    DERIVED,
    RELATED
}


