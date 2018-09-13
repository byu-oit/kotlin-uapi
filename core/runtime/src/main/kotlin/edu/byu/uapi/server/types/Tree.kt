package edu.byu.uapi.server.types

@InternalInterface
enum class TreeNodeType {
    OBJECT, LIST, STRING, INTEGER, DECIMAL, BOOLEAN, NULL
}

@InternalInterface
sealed class TreeNode {
    abstract val type: TreeNodeType
}

@InternalInterface
abstract class ObjectNode : TreeNode(),
                            Map<String, TreeNode> {
    override val type: TreeNodeType = TreeNodeType.OBJECT

    abstract fun add(
        key: String,
        node: TreeNode
    ): ObjectNode

    abstract fun add(
        key: String,
        value: String?
    ): ObjectNode

    abstract fun add(
        key: String,
        value: Int?
    ): ObjectNode

    abstract fun add(
        key: String,
        value: Float?
    ): ObjectNode

    abstract fun add(
        key: String,
        value: Boolean?
    ): ObjectNode

    abstract fun addObject(
        key: String
    ): ObjectNode

    abstract fun addNull(key: String): ObjectNode
}

@InternalInterface
interface ScalarNode<Type> {
    abstract val value: Type
}

@InternalInterface
abstract class StringNode : TreeNode(),
                            ScalarNode<String> {
    override val type = TreeNodeType.STRING
}

@InternalInterface
abstract class IntegerNode : TreeNode(),
                             ScalarNode<Int> {

    override val type = TreeNodeType.INTEGER
}

@InternalInterface
abstract class DecimalNode : TreeNode(),
                             ScalarNode<Float> {

    override val type = TreeNodeType.DECIMAL
}

@InternalInterface
abstract class BooleanNode : TreeNode(),
                             ScalarNode<Boolean> {

    override val type = TreeNodeType.BOOLEAN
}

@InternalInterface
abstract class NullNode : TreeNode(),
                          ScalarNode<Nothing> {

    override val type = TreeNodeType.NULL
}
