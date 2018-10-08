package edu.byu.uapi.kotlin.examples.library.infra.db

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

object DB {

    private val schema = "LIBRARY"

    private var initialized = false

    private val pool by lazy {
        val ds = HikariDataSource()
        ds.jdbcUrl = "jdbc:h2:./target/library/library-db"
        ds.username = "sa"
        ds.password = ""
        ds
    }

    private fun ensureInitialized(connection: Connection) {
        if (initialized) return
        val hasSchema = connection.prepareStatement("select count(*) from information_schema.schemata where schema_name = ?").use { ps ->
            ps.setString(1, schema)
            ps.executeQuery().use { rs ->
                rs.first()
                val result = rs.getInt(1)
                result == 1
            }
        }
        if (!hasSchema) {
            connection.prepareCall("runscript from 'classpath:/sql/init.sql'").use {
                it.execute()
            }
        }
        initialized = true
    }

    fun openConnection(): Connection {
        val conn = pool.connection

        ensureInitialized(conn)

        return conn
    }

}

inline fun <T> DB.query(sql: String, prepare: PreparedStatement.() -> Unit, process: ResultSet.() -> T ): T {
    return DB.openConnection().use { conn ->
        conn.prepareStatement(sql).use { ps ->
            ps.prepare()
            ps.executeQuery().use(process)
        }
    }
}

inline fun <T> DB.query(sql: String, process: ResultSet.() -> T ) = this.query(sql, {}, process)

inline fun <T> DB.queryList(sql: String, prepare: PreparedStatement.() -> Unit, process: ResultSet.() -> T): List<T> {
    return DB.query(sql, prepare) {
        val list = mutableListOf<T>()

        while(next()) {
            list.add(process())
        }

        list
    }
}

inline fun <T> DB.queryList(sql: String, process: ResultSet.() -> T ) = this.queryList(sql, {}, process)

inline fun <T: Any> DB.querySingle(sql: String, prepare: PreparedStatement.() -> Unit, process: ResultSet.() -> T): T? {
    return query(sql, prepare) {
        if (next()) {
            process()
        } else {
            null
        }
    }
}

inline fun <T: Any> DB.queryAlwaysSingle(sql: String, prepare: PreparedStatement.() -> Unit, process: ResultSet.() -> T): T {
    return query(sql, prepare) {
        first()
            process()
    }
}

fun main(args: Array<String>) {
    DB.openConnection().use {
        it.prepareStatement("select count(*) from library.BOOK").executeQuery().use { rs ->
            rs.first()
            println("Ran!")
            println(rs.getInt(1))
        }
    }
}
