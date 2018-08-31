package edu.byu.uapi.server.schemas

sealed class InputSchema {
    abstract val nullable: Boolean
    abstract val docs: String

    //TODO: Extend with more info (pattern, min/max, etc)
}

data class ScalarInputSchema(
    val type: UAPIScalarType,
    override val nullable: Boolean,
    override val docs: String
) : InputSchema()

data class ObjectInputSchema(
    val fields: Map<String, InputSchema>,
    override val nullable: Boolean,
    override val docs: String
) : InputSchema()

data class CollectionInputSchema(
    val itemSchema: InputSchema,
    override val nullable: Boolean,
    override val docs: String
) : InputSchema()

enum class UAPIScalarType {
    STRING,
    NUMBER,
    BOOLEAN,
    DATE,
    DATE_TIME
    //TODO: Others?
}
