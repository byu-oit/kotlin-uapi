package edu.byu.uapi.kotlin.examples.library.infra.db

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

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
        val hasSchema =connection.prepareStatement("select count(*) from information_schema.schemata where schema_name = ?")
            .use { ps ->
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

fun main(args: Array<String>) {
    DB.openConnection().use {
        it.prepareStatement("select count(*) from library.BOOK").executeQuery().use { rs ->
            rs.first()
            println("Ran!")
            println(rs.getInt(1))
        }
    }
}
