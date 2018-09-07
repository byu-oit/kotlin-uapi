package edu.byu.uapi.server.response

import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.DescribeSpec
import java.time.*
import java.util.*

class NullableDateTimeFieldDefinitionSpec : DescribeSpec() {

    private val unixTime = -14182940L

    private val instant = Instant.ofEpochSecond(unixTime)

    init {
        describe("fromZonedDateTime") {
            it("should convert the response from getValue") {
                val field = NullableDateTimeFieldDefinition.fromZonedDateTime<User, Long>(
                    name = "field",
                    getValue = ::toZonedDateTime
                )
                val value = field.getValue.invoke(unixTime)
                value shouldBe instant
            }
            context("description") {
                it("should convert the input") {
                    val describer = mockDescriber<ZonedDateTime>()
                    val field = NullableDateTimeFieldDefinition.fromZonedDateTime<User, Long>(
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
                it("should handle a null value") {
                    val describer = mockDescriber<ZonedDateTime>()

                    val field = NullableDateTimeFieldDefinition.fromZonedDateTime<User, Long>(
                        name = "field",
                        getValue = ::toZonedDateTime,
                        description = describer
                    )

                    val result = field.description!!.invoke(unixTime, null)
                    result shouldBe null

                    verify(describer)
                        .invoke(eq(unixTime), isNull())
                }
            }
            context("longDescription") {
                it("should convert the input") {
                    val describer = mockDescriber<ZonedDateTime>()
                    val field = NullableDateTimeFieldDefinition.fromZonedDateTime<User, Long>(
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
                it("should handle a null value") {
                    val describer = mockDescriber<ZonedDateTime>()

                    val field = NullableDateTimeFieldDefinition.fromZonedDateTime<User, Long>(
                        name = "field",
                        getValue = ::toZonedDateTime,
                        longDescription = describer
                    )

                    val result = field.longDescription!!.invoke(unixTime, null)
                    result shouldBe null

                    verify(describer)
                        .invoke(eq(unixTime), isNull())
                }
            }
        }
        describe("fromOffsetDateTime") {
            it("should convert the response from getValue") {
                val field = NullableDateTimeFieldDefinition.fromOffsetDateTime<User, Long>(
                    name = "field",
                    getValue = ::toOffsetDateTime
                )
                val value = field.getValue.invoke(unixTime)
                value shouldBe instant
            }
            context("description") {
                it("should convert the input") {
                    val describer = mockDescriber<OffsetDateTime>()
                    val field = NullableDateTimeFieldDefinition.fromOffsetDateTime<User, Long>(
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
                it("should handle a null value") {
                    val describer = mockDescriber<OffsetDateTime>()

                    val field = NullableDateTimeFieldDefinition.fromOffsetDateTime<User, Long>(
                        name = "field",
                        getValue = ::toOffsetDateTime,
                        description = describer
                    )

                    val result = field.description!!.invoke(unixTime, null)
                    result shouldBe null

                    verify(describer)
                        .invoke(eq(unixTime), isNull())
                }
            }
            context("longDescription") {
                it("should convert the input") {
                    val describer = mockDescriber<OffsetDateTime>()
                    val field = NullableDateTimeFieldDefinition.fromOffsetDateTime<User, Long>(
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
                it("should handle a null value") {
                    val describer = mockDescriber<OffsetDateTime>()

                    val field = NullableDateTimeFieldDefinition.fromOffsetDateTime<User, Long>(
                        name = "field",
                        getValue = ::toOffsetDateTime,
                        longDescription = describer
                    )

                    val result = field.longDescription!!.invoke(unixTime, null)
                    result shouldBe null

                    verify(describer)
                        .invoke(eq(unixTime), isNull())
                }
            }
        }
        describe("fromDate") {
            it("should convert the response from getValue") {
                val field = NullableDateTimeFieldDefinition.fromDate<User, Long>(
                    name = "field",
                    getValue = ::toDate
                )
                val value = field.getValue.invoke(unixTime)
                value shouldBe instant
            }
            context("description") {
                it("should convert the input") {
                    val describer = mockDescriber<Date>()
                    val field = NullableDateTimeFieldDefinition.fromDate<User, Long>(
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
                it("should handle a null value") {
                    val describer = mockDescriber<Date>()

                    val field = NullableDateTimeFieldDefinition.fromDate<User, Long>(
                        name = "field",
                        getValue = ::toDate,
                        description = describer
                    )

                    val result = field.description!!.invoke(unixTime, null)
                    result shouldBe null

                    verify(describer)
                        .invoke(eq(unixTime), isNull())
                }
            }
            context("longDescription") {
                it("should convert the input") {
                    val describer = mockDescriber<Date>()
                    val field = NullableDateTimeFieldDefinition.fromDate<User, Long>(
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
                it("should handle a null value") {
                    val describer = mockDescriber<Date>()

                    val field = NullableDateTimeFieldDefinition.fromDate<User, Long>(
                        name = "field",
                        getValue = ::toDate,
                        longDescription = describer
                    )

                    val result = field.longDescription!!.invoke(unixTime, null)
                    result shouldBe null

                    verify(describer)
                        .invoke(eq(unixTime), isNull())
                }
            }
        }
    }

    private inline fun <reified T : Any> mockDescriber(): Describer<Long, T?> {
        return mock {
            on { invoke(any(), notNull()) } doReturn "desc"
            on { invoke(any(), isNull()) } doReturn null as String?
        }
    }

    private object User

    private fun toZonedDateTime(unix: Long?): ZonedDateTime? = if (unix != null) ZonedDateTime.ofInstant(Instant.ofEpochSecond(unix), ZoneId.systemDefault()) else null
    private fun toOffsetDateTime(unix: Long?): OffsetDateTime? = if (unix != null) ZonedDateTime.ofInstant(Instant.ofEpochSecond(unix), ZoneId.systemDefault()).toOffsetDateTime() else null
    private fun toDate(unix: Long?): Date? = if (unix != null) Date(unix * 1000) else null
}

