package edu.byu.uapi.kotlin.examples.library

import edu.byu.uapi.kotlin.examples.library.infra.db.WhereClause
import java.sql.PreparedStatement

interface Whereable {
    fun toWhereClauses(alias: String): List<WhereClause>
}

data class BookQueryFilters(
    val isbn: Set<String> = emptySet(),
    val title: String? = null,
    val subtitle: String? = null,
    val publisherId: Set<Int> = emptySet(),
    val publisherNames: Set<String> = emptySet(),
    val publicationYear: Int? = null,
    val restricted: Boolean? = null,
    val authors: AuthorQueryFilters? = null,
    val genres: GenreQueryFilters? = null
) : Whereable {
    override fun toWhereClauses(alias: String): List<WhereClause> {
        val q = mutableListOf<WhereClause>()

        if (isbn.isNotEmpty()) {
            q += isbn.toInClause("$alias.isbn", PreparedStatement::setString)
        }
        if (title != null) {
            q += title.toEqualsClause("$alias.title", PreparedStatement::setString)
        }
        if (subtitle != null) {
            q += WhereClause.singleParam("EXISTS (select 1 from library.book_subtitles bs where bs.subtitle = ? and bs.book_id = $alias.book_id)", subtitle, PreparedStatement::setString)
        }
        if (publisherId.isNotEmpty()) {
            q += publisherId.toInClause("$alias.publisher_id", PreparedStatement::setInt)
        }
        if (publisherNames.isNotEmpty()) {
            q += publisherNames.toCustomInClause({ placeholders -> "exists (select 1 from library.publishers p where p.publisher_id = $alias.publisher_id and p.common_name in $placeholders)" }, PreparedStatement::setString)
        }
        if (publicationYear != null) {
            q += publicationYear.toEqualsClause("$alias.publication_year", PreparedStatement::setInt)
        }
        if (restricted != null) {
            q += restricted.toEqualsClause("$alias.restricted", PreparedStatement::setBoolean)
        }
        if (authors != null) {
            val authorsSubquery = authors.toExistsClause("library.author", "a", "a.author_id = ba.author_id")
            q += WhereClause(
                "exists (select 1 from library.book_authors ba where ${authorsSubquery.clause} and $alias.book_id = ba.book_id )",
                setValues = authorsSubquery.setValues
            )
        }
        if (genres != null) {
            val genresSubquery = genres.toExistsClause("library.genre", "g", "g.genre_code = bg.genre_code")
            q += WhereClause(
                "exists (select 1 from library.book_genres bg where ${genresSubquery.clause} and $alias.book_id = bg.book_id )",
                setValues = genresSubquery.setValues
            )
        }

        return q
    }
}

fun List<WhereClause>.toWhereStatementOrEmpty(): String {
    return if (this.isEmpty()) {
        ""
    } else {
        "where " + this.joinToString(prefix = "(", separator = ") AND (", postfix = ")") { it.clause }
    }
}

fun Whereable.toExistsClause(
    table: String,
    alias: String,
    joinClauses: String
): WhereClause {
    val nested = this
    val nestedClauses = nested.toWhereClauses(alias) + WhereClause(joinClauses)

    val where = nestedClauses.toWhereStatementOrEmpty()

    return WhereClause(
        "exists (select 1 from $table $alias $where)"
    ) { startIndex ->
        var idx = 0
        nestedClauses.forEach {
            val set = it.setValues
            if (set != null) {
                idx += set(startIndex + idx)
            }
        }
        idx
    }
}

fun <T : Any> T.toEqualsClause(
    column: String,
    set: PreparedStatement.(index: Int, value: T) -> Unit
): WhereClause {
    val value = this
    return WhereClause.singleParam(
        "$column = ?",
        value,
        set
    )
}

fun <T : Any> Collection<T>.toInClause(
    column: String,
    set: PreparedStatement.(index: Int, value: T) -> Unit
): WhereClause {
    return this.toCustomInClause({ placeholders -> "$column IN $placeholders" }, set)
}

fun <T : Any> Collection<T>.toCustomInClause(
    buildClause: (placeholders: String) -> String,
    set: PreparedStatement.(index: Int, value: T) -> Unit
): WhereClause {
    val collection = this
    val placeholders = (0 until this.size).joinToString { "?" }
    return WhereClause(
        buildClause("($placeholders)")
    ) { startIndex ->
        collection.forEachIndexed { index, value ->
            this.set(startIndex + index, value)
        }
        collection.size
    }
}

data class AuthorQueryFilters(
    val id: Set<Int> = emptySet(),
    val name: Set<String> = emptySet()
) : Whereable {
    override fun toWhereClauses(alias: String): List<WhereClause> {
        val q = mutableListOf<WhereClause>()
        if (id.isNotEmpty()) {
            q += id.toInClause("$alias.author_id", PreparedStatement::setInt)
        }
        if (name.isNotEmpty()) {
            q += name.toInClause("$alias.name", PreparedStatement::setString)
        }
        return q
    }
}

data class GenreQueryFilters(
    val code: Set<String> = emptySet(),
    val name: Set<String> = emptySet()
) : Whereable {
    override fun toWhereClauses(alias: String): List<WhereClause> {
        val q = mutableListOf<WhereClause>()
        if (code.isNotEmpty()) {
            q += code.toInClause("$alias.genre_code", PreparedStatement::setString)
        }
        if (name.isNotEmpty()) {
            q += name.toInClause("$alias.name", PreparedStatement::setString)
        }
        return q
    }
}

sealed class BookSearch : Whereable {
    abstract val text: String
    abstract fun clausesToOr(
        alias: String,
        wildcardText: String
    ): List<WhereClause>

    final override fun toWhereClauses(alias: String): List<WhereClause> {
        val withWildcards = "%${text.toLowerCase()}%"
        return listOf(
            WhereClause.or(clausesToOr(alias, withWildcards))
        )
    }
}

data class BookTitleSearch(override val text: String) : BookSearch() {
    override fun clausesToOr(
        alias: String,
        wildcardText: String
    ): List<WhereClause> {
        return listOf(
            WhereClause.singleParam("lower($alias.title) like ?", wildcardText, PreparedStatement::setString),
            WhereClause.singleParam("exists (select 1 from library.book_subtitles bs where lower(bs.subtitle) like ? and bs.book_id = $alias.book_id)", wildcardText, PreparedStatement::setString)
        )
    }
}

data class BookAuthorSearch(override val text: String) : BookSearch() {
    override fun clausesToOr(
        alias: String,
        wildcardText: String
    ): List<WhereClause> {
        return listOf(
            WhereClause.singleParam("""
                exists (
                  select 1 from library.book_authors ba where ba.book_id = $alias.book_id and exists(
                    select 1 from library.author a where a.author_id = ba.author_id and lower(name) like ?
                  )
                )
                """.trimIndent(), wildcardText, PreparedStatement::setString)
        )
    }
}

data class BookGenreSearch(override val text: String) : BookSearch() {
    override fun clausesToOr(
        alias: String,
        wildcardText: String
    ): List<WhereClause> {
        return listOf(
            WhereClause("""
                exists(
                  select 1 from library.book_genres bg where bg.book_id = $alias.book_id and exists(
                    select 1 from library.genre g where g.genre_code = bg.genre_code and
                     ( lower(g.genre_code) like ? OR lower(g.name) like ? )
                  )
                )
            """.trimIndent()) { startIndex ->
                setString(startIndex, wildcardText)
                //yay positional parameters...🙃
                setString(startIndex + 1, wildcardText)
                2
            }
        )
    }
}

data class BookControlNumbersSearch(override val text: String) : BookSearch() {
    override fun clausesToOr(
        alias: String,
        wildcardText: String
    ): List<WhereClause> {
        return listOf(
            WhereClause.singleParam("lower($alias.isbn) = ?", wildcardText, PreparedStatement::setString),
            WhereClause.singleParam("$alias.oclc = ?", wildcardText, PreparedStatement::setString)
        )
    }
}

//TITLES,
//AUTHORS,
//GENRES,
//CONTROL_NUMBERS;
