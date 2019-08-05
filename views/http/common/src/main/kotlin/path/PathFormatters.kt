package edu.byu.uapi.server.http.path

/**
 * Common path formatter definitions
 */
object PathFormatters {
    /**
     * Formats parameters like "/:foo/:bar,:baz"
     */
    val COLON: PathFormatter = SimplePathFormatter(":")

    /**
     * Formats parameters like "/{foo}/{bar},{baz}"
     */
    val CURLY_BRACE: PathFormatter =
        SimplePathFormatter("{", "}")

    /**
     * Compound-variable-flattening version of [COLON].
     * @see CompoundFlatteningFormatter
     */
    val FLAT_COLON: PathFormatter =
        CompoundFlatteningFormatter(":")

    /**
     * Compound-variable-flattening version of [CURLY_BRACE].
     * @see CompoundFlatteningFormatter
     */
    val FLAT_CURLY_BRACE: PathFormatter =
        CompoundFlatteningFormatter("{", "}")
}
