package edu.byu.uapi.server.schemas

data class ResponseModel(
    val properties: List<ResponseProperty>
)

data class ResponseProperty(
    val name: String,
    val type: UAPIScalarType,
    val nullable: Boolean
//TODO: Extend with more info (pattern, min/max, etc)
)
