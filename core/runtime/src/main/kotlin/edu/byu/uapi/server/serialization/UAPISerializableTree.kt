package edu.byu.uapi.server.serialization

interface UAPISerializable<Strategy: SerializationStrategy> {
    fun serialize(strategy: Strategy)
}

interface UAPISerializableTree: UAPISerializable<TreeSerializationStrategy>

interface UAPISerializableValue: UAPISerializable<ValueSerializationStrategy>

interface SerializationStrategy

