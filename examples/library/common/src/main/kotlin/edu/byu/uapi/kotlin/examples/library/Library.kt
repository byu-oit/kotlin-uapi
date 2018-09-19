package edu.byu.uapi.kotlin.examples.library

import edu.byu.uapi.kotlin.examples.library.infra.db.DB
import java.sql.ResultSet

/**
 * Created by Scott Hutchings on 9/14/2018.
 * kotlin-uapi-dsl-pom
 */
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

    fun getBook(byOclc: Long): Book? {
        val sql = "select * from library.BOOK where oclc = ?"
        DB.openConnection().use { connection ->
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setLong(1, byOclc)
                preparedStatement.executeQuery().use { resultSet ->
                    return if (resultSet.first()) {
                        convertResultSetToBook(resultSet)
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun searchForBooks(byTitle: String): List<Book> {
        val sql = "select b.oclc, b.isbn, b.title, b.subtitles, b.published_year, p.publisher_id, p.name " +
            "from library.BOOK b, library.publisher p " +
            "where b.publisher = p.publisher_id " +
            "and b.title like ?"
        DB.openConnection().use { connection ->
            connection.prepareStatement(sql).use { preparedStatement ->
                preparedStatement.setString(1, "%$byTitle%")
                preparedStatement.executeQuery().use { resultSet ->
                    val books = mutableListOf<Book>()
                    while (resultSet.next()) {
                        books.add(convertResultSetToBook(resultSet))
                    }
                    return books
                }
            }
        }
    }

    fun getPublisher(byPublisherId: Int): Publisher? {
        DB.openConnection().use { connection ->
            connection.prepareStatement("select * from library.publisher where publisher_id = ?").use { preparedStatement ->
                preparedStatement.setInt(1, byPublisherId)
                preparedStatement.executeQuery().use { resultSet ->
                    return if (resultSet.first()) {
                        Publisher(resultSet.getInt("publisher_id"), resultSet.getString("name"))
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun getAuthors(forOclc: Long): List<Author> {
        DB.openConnection().use { connection ->
            connection.prepareStatement("select a.* from library.author a, library.book_authors ba where ba.author_id = a.author_id and ba.oclc = ?").use { preparedStatement ->
                preparedStatement.setLong(1, forOclc)
                preparedStatement.executeQuery().use { resultSet ->
                    val authors = mutableListOf<Author>()
                    while (resultSet.next()) {
                        authors.add(Author(resultSet.getInt("author_id"), resultSet.getString("name")))
                    }
                    return authors
                }
            }
        }
    }

    fun getGenres(forOclc: Long): List<Genre> {
        DB.openConnection().use { connection ->
            connection.prepareStatement("select g.* from library.genre g, library.book_genres bg where bg.genre_id = g.genre_id and bg.oclc = ?").use { preparedStatement ->
                preparedStatement.setLong(1, forOclc)
                preparedStatement.executeQuery().use { resultSet ->
                    val genres = mutableListOf<Genre>()
                    while (resultSet.next()) {
                        genres.add(Genre(resultSet.getInt("genre_id"), resultSet.getString("name")))
                    }
                    return genres
                }
            }
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
        DB.openConnection().use { conn ->
            conn.prepareStatement("select cardholder_id from library.CARDHOLDER where net_id = ?").use { ps ->
                ps.setString(1, netId)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        rs.getInt(1)
                    } else {
                        null
                    }
                }
            }

        }

    private fun convertResultSetToBook(rs: ResultSet): Book {
        val publisher = getPublisher(rs.getInt("publisher_id"))
        val oclc = rs.getLong("oclc")
        return Book(oclc,
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("subtitles"),
                    rs.getInt("published_year"),
//            Publisher(rs.getInt("publisher_id"), rs.getString("name")),
                    publisher!!,
                    getAuthors(oclc),
                    getGenres(oclc)
        )
    }
}

fun main(args: Array<String>) {
    val books = Library.searchForBooks("Way")
    println(books)
}
