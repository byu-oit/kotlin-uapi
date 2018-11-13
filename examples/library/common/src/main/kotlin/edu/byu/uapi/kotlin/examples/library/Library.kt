package edu.byu.uapi.kotlin.examples.library

import edu.byu.uapi.kotlin.examples.library.infra.db.*
import java.sql.ResultSet

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

    fun getAuthors(bookId: Long): List<Author> =
        DB.queryList("select a.*, ba.author_order from library.author a, library.book_authors ba where ba.author_id = a.author_id and ba.book_id = ? order by ba.author_order",
                     { setLong(1, bookId) }) {
            Author(getInt("author_id"), getString("name"), getInt("author_order"))
        }

    fun getGenres(bookId: Long): List<Genre> =
        DB.queryList("select g.* from library.genre g, library.book_genres bg where bg.genre_code = g.genre_code and bg.book_id = ?",
                     { setLong(1, bookId) }) {
            Genre(getString("genre_code"), getString("name"))
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
                    getAuthors(id),
                    getGenres(id),
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
}


fun main(args: Array<String>) {
    println(Library.listBooks(includeRestricted = true, filters = BookQueryFilters(
        title = "The Player of Games"
    )))
}
