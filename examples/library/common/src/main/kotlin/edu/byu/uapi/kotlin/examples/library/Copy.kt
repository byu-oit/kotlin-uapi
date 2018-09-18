package edu.byu.uapi.kotlin.examples.library

/**
 * Created by Scott Hutchings on 8/31/2018.
 * kotlin-uapi-dsl-pom
 */
class Copy (val copyId: Int,
            val book: Book,
            var checkedOutHistory: List<CheckedOutCopy>) {
    val currentCheckedOutCopy: CheckedOutCopy?
        get() = checkedOutHistory.firstOrNull { it.returnedDateTime == null }
    val isCheckedOut: Boolean
        get() = checkedOutHistory.isEmpty() || checkedOutHistory.any { it.returnedDateTime == null }

}
