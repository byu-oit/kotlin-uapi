package edu.byu.uapi.server.response

import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.DescribeSpec
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class DateTimeFieldDefinitionSpec : DescribeSpec() {

    private val unixTime = -14182940L

    private val instant = Instant.ofEpochSecond(unixTime)

    init {
        describe("fromZonedDateTime") {
            it("should convert the response from getValue") {
                val field = DateTimeFieldDefinition.fromZonedDateTime<User, Long>(
                    name = "field",
                    getValue = ::toZonedDateTime
                )
                val value = field.getValue.invoke(unixTime)
                value shouldBe instant
            }
            it("should convert the input to description") {
                val describer = mockDescriber<ZonedDateTime>()
                val field = DateTimeFieldDefinition.fromZonedDateTime<User, Long>(
                    name = "field",
                    getValue = ::toZonedDateTime,
                    description = describer
                )
                field.description shouldNotBe null
                val result = field.description!!.invoke(unixTime, instant)
                result shouldBe "desc"

                verify(describer)
                    .invoke(eq(unixTime), argThat { this.toEpochSecond() == unixTime })
            }
            it("should convert the input to longDescription") {
                val describer = mockDescriber<ZonedDateTime>()
                val field = DateTimeFieldDefinition.fromZonedDateTime<User, Long>(
                    name = "field",
                    getValue = ::toZonedDateTime,
                    longDescription = describer
                )
                field.longDescription shouldNotBe null
                val result = field.longDescription!!.invoke(unixTime, instant)
                result shouldBe "desc"

                verify(describer)
                    .invoke(eq(unixTime), argThat { this.toEpochSecond() == unixTime })
            }
        }
        describe("fromOffsetDateTime") {
            it("should convert the response from getValue") {
                val field = DateTimeFieldDefinition.fromOffsetDateTime<User, Long>(
                    name = "field",
                    getValue = ::toOffsetDateTime
                )
                val value = field.getValue.invoke(unixTime)
                value shouldBe instant
            }
            it("should convert the input to description") {
                val describer = mockDescriber<OffsetDateTime>()
                val field = DateTimeFieldDefinition.fromOffsetDateTime<User, Long>(
                    name = "field",
                    getValue = ::toOffsetDateTime,
                    description = describer
                )
                field.description shouldNotBe null
                val result = field.description!!.invoke(unixTime, instant)
                result shouldBe "desc"

                verify(describer)
                    .invoke(eq(unixTime), argThat { this.toEpochSecond() == unixTime })
            }
            it("should convert the input to longDescription") {
                val describer = mockDescriber<OffsetDateTime>()
                val field = DateTimeFieldDefinition.fromOffsetDateTime<User, Long>(
                    name = "field",
                    getValue = ::toOffsetDateTime,
                    longDescription = describer
                )
                field.longDescription shouldNotBe null
                val result = field.longDescription!!.invoke(unixTime, instant)
                result shouldBe "desc"

                verify(describer)
                    .invoke(eq(unixTime), argThat { this.toEpochSecond() == unixTime })
            }
        }
        describe("fromDate") {
            it("should convert the response from getValue") {
                val field = DateTimeFieldDefinition.fromDate<User, Long>(
                    name = "field",
                    getValue = ::toDate
                )
                val value = field.getValue.invoke(unixTime)
                value shouldBe instant
            }
            it("should convert the input to description") {
                val describer = mockDescriber<Date>()
                val field = DateTimeFieldDefinition.fromDate<User, Long>(
                    name = "field",
                    getValue = ::toDate,
                    description = describer
                )
                field.description shouldNotBe null
                val result = field.description!!.invoke(unixTime, instant)
                result shouldBe "desc"

                verify(describer)
                    .invoke(eq(unixTime), argThat { this.time / 1000 == unixTime })
            }
            it("should convert the input to longDescription") {
                val describer = mockDescriber<Date>()
                val field = DateTimeFieldDefinition.fromDate<User, Long>(
                    name = "field",
                    getValue = ::toDate,
                    longDescription = describer
                )
                field.longDescription shouldNotBe null
                val result = field.longDescription!!.invoke(unixTime, instant)
                result shouldBe "desc"

                verify(describer)
                    .invoke(eq(unixTime), argThat { this.time / 1000 == unixTime })
            }
        }
    }

    private inline fun <reified T : Any> mockDescriber(): Describer<Long, T> {
        return mock {
            on { invoke(anyOrNull(), anyOrNull()) } doReturn "desc"
        }
    }


    private object User

    private fun toZonedDateTime(unix: Long): ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(unix), ZoneId.systemDefault())
    private fun toOffsetDateTime(unix: Long): OffsetDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(unix), ZoneId.systemDefault()).toOffsetDateTime()
    private fun toDate(unix: Long): Date = Date(unix * 1000)

}


