package edu.byu.uapi.kotlin.examples.library

class Book (val oclc: Long,
            val isbn: String? = null,
            val title: String,
            val subtitles: String? = null,
            val publishedYear: Int,
            val publisher: Publisher,
            val authors: List<Author>,
            val genres: List<Genre>) {

    override fun toString(): String {
        return "Book(oclc=$oclc, isbn=$isbn, title='$title', subtitles=$subtitles, publishedYear=$publishedYear, publisher=$publisher, authors=$authors, genres=$genres)"
    }


}
