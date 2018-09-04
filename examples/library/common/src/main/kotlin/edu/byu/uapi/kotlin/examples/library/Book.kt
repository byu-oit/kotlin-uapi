package edu.byu.uapi.kotlin.examples.library

class Book (val oclc: Long,
            val isbn: String? = null,
            val title: String,
            val subtitles: String? = null,
            val publishedYear: Int,
            val publisher: Publisher,
            val authors: Set<Author>,
            val genres: Set<Genre>,
            val copies: Set<Copy>) {

}
