package edu.byu.uapi.kotlin.examples.library

class Book(
    val id: Long,
    val oclc: Long,
    val isbn: String? = null,
    val title: String,
    val subtitles: List<String> = emptyList(),
    val publishedYear: Int,
    val publisher: Publisher,
    val authors: List<Author>,
    val genres: List<Genre>,
    val availableCopies: Int
) {

    override fun toString(): String {
        return "Book(id=$id, oclc=$oclc, isbn=$isbn, title='$title', subtitles=$subtitles, publishedYear=$publishedYear, publisher=$publisher, authors=$authors, genres=$genres, availableCopies=$availableCopies)"
    }


}
