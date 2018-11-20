package edu.byu.uapi.library

import java.time.Year

data class CreateBook(
    val oclc: Long,
    val isbn: String? = null,
    val title: String,
    val subtitles: List<String> = emptyList(),
    val publishedYear: Year,
    val publisherId: Int,
    val authorIds: List<Long>,
    val genreCodes: Set<String> = emptySet(),
    val restricted: Boolean = false
)
