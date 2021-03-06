package edu.byu.uapi.kotlin.examples.library

data class Book(
    val id: Long,
    val oclc: OCLCNumber,
    val isbn: String? = null,
    val title: String,
    val subtitles: List<String> = emptyList(),
    val publishedYear: Int,
    val publisher: Publisher,
    val authors: List<Author>,
    val genres: Set<Genre>,
    val availableCopies: Int,
    val restricted: Boolean
)

data class OCLCNumber(val oclc: Long)
