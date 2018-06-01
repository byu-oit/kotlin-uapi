package edu.byu.uapidsl.model

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.typemodeling.OutputSchema

data class ResponseModel<Type: Any>(
    val schema: OutputSchema,
    val writer: ObjectWriter
)

