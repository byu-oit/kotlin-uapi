package edu.byu.uapi.kotlin.examples.library

import edu.byu.uapi.kotlin.examples.library.infra.db.*
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement
import java.time.LocalTime

/**
 * Created by Scott Hutchings on 9/14/2018.
 * kotlin-uapi-dsl-pom
 */

private val DB = DefaultDB

object Library {
//    fun searchForBooks(byOclc: Long? = null,
//                       byIsbn: String? = null,
//                       byTitle: String? = null,
//                       bySubTitle: String? = null,
//                       byPublishedYear: Int? = null,
//                       byPublisher: Publisher? = null,
//                       byAuthors: List<Author>? = null,
//                       byGenres: List<Genre>? = null): List<Book> {
//        var searchSqlStatement = "select * from library.BOOK where "
//        if (byOclc != null ) searchSqlStatement += "oclc = "
//        DB.openConnection().use { connection ->
//            connection.prepareStatement(searchSqlStatement).executeQuery().use { resultSet ->
//
//            }
//        }
//        return emptyList()
//    }

    fun getBookByOclc(byOclc: Long): Book? {
        val sql = "select * from library.BOOK where oclc = ?"
        return DB.querySingle(sql, { setLong(1, byOclc) }, this::convertResultSetToBook)
    }

    fun getBookById(bookId: Long): Book? {
        val sql = "select * from library.BOOK where book_id = ?"
        return DB.querySingle(sql, { setLong(1, bookId) }, this::convertResultSetToBook)
    }

    private val BASIC_BOOK_SELECT = """
        select b.*
        from library.book b
        """.trimIndent()

    fun searchForBooks(byTitle: String): List<Book> {
        return DB.queryList("$BASIC_BOOK_SELECT where b.title like ?",
                            { setString(1, "%$byTitle%") },
                            this::convertResultSetToBook)
    }

    fun getPublisher(byPublisherId: Int): Publisher? {
        return DB.querySingle("select * from library.publisher where publisher_id = ?",
                              { setInt(1, byPublisherId) }) {
            Publisher(getInt("publisher_id"), getString("common_name"), getString("full_name"))
        }
    }

    fun getAuthorsForBook(bookId: Long): List<Author> =
        DB.queryList("select a.*, ba.author_order from library.author a, library.book_authors ba where ba.author_id = a.author_id and ba.book_id = ? order by ba.author_order",
                     { setLong(1, bookId) }) {
            Author(getInt("author_id"), getString("name"), getInt("author_order"))
        }

    fun getAuthor(authorId: Long): Author? =
        DB.querySingle("select a.* from library.author a where a.author_id = ?",
                       { setLong(1, authorId) }) {
            Author(getInt("author_id"), getString("name"), 1)
        }

    fun getGenresForBook(bookId: Long): Set<Genre> =
        DB.queryList("select g.* from library.genre g, library.book_genres bg where bg.genre_code = g.genre_code and bg.book_id = ?",
                     { setLong(1, bookId) }) {
            Genre(getString("genre_code"), getString("name"))
        }.toSet()

    fun getGenreByCode(code: String): Genre? {
        return DB.querySingle(
            "select g.* from library.genre g where g.genre_code = ?",
            { setString(1, code) }
        ) {
            Genre(getString(1), getString(2))
        }
    }
//
//    fun getCardholderByNetId(netId: String): CardHolder? {
//        return DB.openConnection().use { conn ->
//            conn
//                .prepareStatement("select cardholder_id, net_id, name from library.cardholder where net_id = ?")
//                .use { ps ->
//                    ps.setString(1, netId)
//                    ps.executeQuery().use {
//                        if (it.next()) {
//                            val id = it.getInt(1)
//                            val nid = it.getString(2)
//                            val name = it.getString(3)
//
//                            val history = emptyList<CheckedOutCopy>() // There's a circular dependency here to be sorted otu
//
//                            CardHolder(id, nid, name, history)
//                        } else {
//                            null
//                        }
//                    }
//                }
//        }
//    }

    fun getCardholderIdForNetId(netId: String): Int? =
        DB.querySingle("select cardholder_id from library.CARDHOLDER where net_id = ?",
                       { setString(1, netId) },
                       { getInt(1) }
        )

    private fun convertResultSetToBook(rs: ResultSet): Book {
        val publisher = getPublisher(rs.getInt("publisher_id"))
        val id = rs.getLong("book_id")
        return Book(id,
                    rs.getLong("oclc"),
                    rs.getString("isbn"),
                    rs.getString("title"),
                    getSubtitles(id),
                    rs.getInt("published_year"),
                    publisher!!,
                    getAuthorsForBook(id),
                    getGenresForBook(id),
                    availableCopiesOf(id),
                    rs.getBoolean("restricted")
        )
    }

    fun availableCopiesOf(bookId: Long): Int =
        DB.queryAlwaysSingle(
            "select count(copies.*) from library.book_copy copies " +
                "where copies.book_id = ? " +
                "and not exists (" +
                "select 1 from library.loans l where l.copy_id = copies.copy_id and l.reshelved <> TRUE" +
                ")",
            { setLong(1, bookId) },
            { getInt(1) }
        )

    fun listBooks(
        includeRestricted: Boolean,
        filters: BookQueryFilters? = null,
        search: BookSearch? = null,
        subsetStart: Int = 0,
        subsetSize: Int = Integer.MIN_VALUE,
        sortColumns: List<BookSortableColumns> = emptyList(),
        sortAscending: Boolean = true
    ): ListResult<Book> {
        val sortOrder = if (sortAscending) "asc" else "desc"
        val sorts = if (sortColumns.isNotEmpty()) {
            sortColumns.joinToString(", ") { "${getBookSort(it, "b")} $sortOrder" }
        } else {
            "b.book_id asc"
        }
        val whereClauses = mutableListOf<WhereClause>()
        if (!includeRestricted) {
            whereClauses + WhereClause("restricted = false")
        }
        if (filters != null) {
            whereClauses += filters.toWhereClauses("b")
        }
        if (search != null) {
            whereClauses += search.toWhereClauses("b")
        }

        return DB.queryWithTotal(
            select = "b.*",
            tableIsh = "library.book b",
            where = whereClauses,
            order = sorts,
            limit = subsetSize,
            offset = subsetStart,
            process = this::convertResultSetToBook
        )
    }


    private fun getBookSort(
        col: BookSortableColumns,
        alias: String
    ): String = when (col) {
        // The subselects in here are normally a Bad Idea. But this is a toy app, so it's a lot easier
        //  to do this very non-performant sorting.
        BookSortableColumns.OCLC -> "b.oclc"
        BookSortableColumns.TITLE -> "b.title"
        BookSortableColumns.PUBLISHER_NAME -> "(select p.name from library.publisher p where p.publisher_id = b.publisher_id)"
        //Oh my this query is a terrible idea. I beg you, do what I say, not what I do!
        BookSortableColumns.FIRST_AUTHOR_NAME -> "(select a.name from library.author a where exists(select 1 from library.BOOK_AUTHORS ba where a.AUTHOR_ID = ba.AUTHOR_ID and b.book_id = ba.book_id and ba.author_order = 1))"
        BookSortableColumns.ISBN -> "b.isbn"
        BookSortableColumns.PUBLISHED_YEAR -> "b.published_year"
    }

    private fun getSubtitles(bookId: Long): List<String> {
        return DB.queryList(
            "select subtitle from library.book_subtitles where book_id = ? order by subtitle_order asc",
            { setLong(1, bookId) }
        ) {
            getString("subtitle")
        }
    }

    fun createBook(book: NewBook): Book {
        return DB.openConnection().use { conn ->
            conn.prepareStatement("""
                insert into library.book
                (oclc, isbn, title, published_year, publisher_id, restricted)
                values
                (?,    ?,    ?,     ?,              ?,            ?)
            """.trimIndent(), Statement.RETURN_GENERATED_KEYS).use { ps ->
                ps.setLong(1, book.oclc)
                ps.setString(2, book.isbn)
                ps.setString(3, book.title)
                ps.setInt(4, book.publishedYear)
                ps.setInt(5, book.publisher.id)
                ps.setBoolean(6, book.restricted)
                ps.execute()
                val id = ps.generatedKeys.use { rs ->
                    rs.next()
                    rs.getLong(1)
                }

                setBookAuthors(conn, id, book.authors)
                setBookGenres(conn, id, book.genres)
                setBookSubtitles(conn, id, book.subtitles)

                getBookById(id)!!
            }
        }
    }

    fun setBookAuthors(
        bookId: Long,
        authors: List<Author>
    ) {
        DB.openConnection().use { c ->
            setBookAuthors(c, bookId, authors)
        }
    }

    internal fun setBookSubtitles(
        connection: Connection,
        bookId: Long,
        subtitles: List<String>
    ) {
        connection.prepareStatement("delete from library.BOOK_SUBTITLES where BOOK_ID = ?").use { delete ->
            delete.setLong(1, bookId)
            delete.execute()
        }
        if (subtitles.isEmpty()) {
            return
        }
        connection.prepareStatement("""
                |insert into library.BOOK_SUBTITLES
                |(BOOK_ID, SUBTITLE_ORDER, SUBTITLE)
                |values (?, ?, ?)""".trimMargin()).use { insert ->
            subtitles.forEachIndexed { idx, s ->
                insert.setLong(1, bookId)
                insert.setInt(2, idx)
                insert.setString(3, s)
                insert.addBatch()
            }
            insert.executeBatch()
        }
    }

    internal fun setBookAuthors(
        connection: Connection,
        bookId: Long,
        authors: List<Author>
    ) {
        connection.prepareStatement("delete from library.BOOK_AUTHORS where BOOK_ID = ?").use { delete ->
            delete.setLong(1, bookId)
            delete.execute()
        }
        if (authors.isEmpty()) {
            return
        }
        connection.prepareStatement("""
                |insert into library.BOOK_AUTHORS
                |(BOOK_ID, AUTHOR_ID, AUTHOR_ORDER)
                |values (?, ?, ?)""".trimMargin()).use { insert ->
            authors.forEachIndexed { idx, a ->
                insert.setLong(1, bookId)
                insert.setInt(2, a.authorId)
                insert.setInt(3, idx)
                insert.addBatch()
            }
            insert.executeBatch()
        }
    }

    internal fun setBookGenres(
        connection: Connection,
        bookId: Long,
        genres: Collection<Genre>
    ) {
        connection.prepareStatement("delete from library.BOOK_GENRES where BOOK_ID = ?").use { delete ->
            delete.setLong(1, bookId)
            delete.execute()
        }
        if (genres.isEmpty()) {
            return
        }
        connection.prepareStatement("""
                |insert into library.BOOK_GENRES
                |(BOOK_ID, GENRE_CODE)
                |values (?, ?)""".trimMargin()).use { insert ->
            genres.toSet().forEach {
                insert.setLong(1, bookId)
                insert.setString(2, it.code)
                insert.addBatch()
            }
            insert.executeBatch()
        }
    }

    fun updateBook(newBook: NewBook): Book {
        TODO("not implemented")
    }

    fun hasCheckedOutCopies(bookId: Long): Boolean {
        val result = DB.querySingle("""
            select 1 from library.book_copy bc
            where bc.book_id = ?
            and exists(
              select 1 from library.loans l
              where l.copy_id = bc.copy_id
              and returned_datetime is null
            )
        """.trimIndent(), {
            setLong(1, bookId)
        }) {
            getInt(1)
        }
        return result != null && result == 1
    }

    fun deleteBook(id: Long) {
        if (hasCheckedOutCopies(id)) throw IllegalStateException("Can't delete books with checked-out copies!")
        DB.execute("delete from library.book where book_id = ?") {
            setLong(1, id)
        }
    }

}

data class NewBook(
    val oclc: Long,
    val isbn: String?,
    val title: String,
    val subtitles: List<String>,
    val publishedYear: Int,
    val publisher: Publisher,
    val authors: List<Author>,
    val genres: Collection<Genre>,
    val restricted: Boolean
)


fun main(args: Array<String>) {
    println(Library.listBooks(includeRestricted = true, filters = BookQueryFilters(
        title = "The Player of Games"
    )))
    val result = Library.createBook(NewBook(
        oclc = LocalTime.now().toSecondOfDay().toLong(),
        isbn = null,
        title = "Title2",
        subtitles = emptyList(),
        publishedYear = 1999,
        publisher = Library.getPublisher(1)!!,
        authors = listOf(Library.getAuthor(1)!!),
        genres = emptySet(),
        restricted = false
    ))
    println(result)
    Library.deleteBook(11)
}
