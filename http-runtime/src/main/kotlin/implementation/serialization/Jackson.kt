package edu.byu.uapidsl.http.implementation.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import edu.byu.uapidsl.types.jackson.JacksonUAPITypesModule

val jacksonJsonMapper = ObjectMapper()
    .registerModule(JacksonUAPITypesModule())
