package edu.byu.uapi.kotlin.examples.library

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Created by Scott Hutchings on 8/31/2018.
 * kotlin-uapi-dsl-pom
 */
class CheckedOutCopy(val checkedOutId: Int,
                     val copy: Copy,
                     val cardHolder: CardHolder,
                     val checkedOutDatetime: LocalDateTime = LocalDateTime.now(),
                     val dueDate: LocalDate = checkedOutDatetime.plusDays(30).toLocalDate(),
                     val returnedDateTime: LocalDateTime? = null,
                     val reshelved: Boolean = false) {
}
