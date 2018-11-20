package edu.byu.uapi.http.json

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Instant

data class TestType(
    val string: String,
    val withDef: String? = "hello",
    @get:JsonProperty(required = false)
    @JsonPropertyDescription("an instant")
    val instant: Instant = Instant.now()
)

fun main() {
    val objectMapper = ObjectMapper()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false)
        .registerModules(
            KotlinModule(),
            JavaTimeModule()
        )

    println(objectMapper.readValue<TestType>("""{"string": "value"}"""))

    println(objectMapper.writeValueAsString(TestType("test")))

    val schemaGen = JsonSchemaGenerator(objectMapper)

    println(objectMapper.writeValueAsString(schemaGen.generateSchema(TestType::class.java)))
}
