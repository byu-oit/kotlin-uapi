package edu.byu.uapi.server.schemas

data class IdentifierModel(
    val fields: List<IdentifierField>
)

data class IdentifierField(
    val name: String,
    val type: UAPIScalarType
//TODO: Extend with more info (pattern, min/max, etc)
)
