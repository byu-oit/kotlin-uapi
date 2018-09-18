package edu.byu.uapi.kotlin.examples.library

/**
 * Created by Scott Hutchings on 8/31/2018.
 * kotlin-uapi-dsl-pom
 */
class CardHolder (val cardholderId: Int,
                  val name: String,
                  val checkedOutHistory: List<CheckedOutCopy>) {
    val currentCheckedOutCopies: List<CheckedOutCopy>
        get() = checkedOutHistory.filter { it.returnedDateTime == null }
}
