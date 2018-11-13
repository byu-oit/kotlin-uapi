package edu.byu.uapi.kotlin.examples.library.infra.db

import com.zaxxer.hikari.HikariDataSource
import edu.byu.uapi.kotlin.examples.library.ListResult
import edu.byu.uapi.kotlin.examples.library.toWhereStatementOrEmpty
import java.nio.file.Files
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class DB(val dbPath: String) {
    private val schema = "LIBRARY"

    private var initialized = false

    private val pool by lazy {
        println("Opening DB at $dbPath (working dir: ${System.getProperty("user.dir")}")
        val ds = HikariDataSource()
        ds.jdbcUrl = "jdbc:h2:$dbPath"
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

val DefaultDB = DB("./target/library/library-db")

inline fun <T> DB.query(
    sql: String,
    prepare: PreparedStatement.() -> Unit,
    process: ResultSet.() -> T
): T {
    println("Executing SQL Query [$sql]")
    return DefaultDB.openConnection().use { conn ->
        conn.prepareStatement(sql).use { ps ->
            ps.prepare()
            ps.executeQuery().use(process)
        }
    }
}

inline fun <T> DB.query(
    sql: String,
    process: ResultSet.() -> T
) = this.query(sql, {}, process)

inline fun <T> DB.queryList(
    sql: String,
    prepare: PreparedStatement.() -> Unit,
    process: ResultSet.() -> T
): List<T> {
    return DefaultDB.query(sql, prepare) {
        val list = mutableListOf<T>()

        while (next()) {
            list.add(process())
        }

        list
    }
}

inline fun <T> DB.queryList(
    sql: String,
    process: ResultSet.() -> T
) = this.queryList(sql, {}, process)

inline fun <T : Any> DB.querySingle(
    sql: String,
    prepare: PreparedStatement.() -> Unit,
    process: ResultSet.() -> T
): T? {
    return query(sql, prepare) {
        if (next()) {
            process()
        } else {
            null
        }
    }
}

inline fun <T : Any> DB.queryAlwaysSingle(
    sql: String,
    prepare: PreparedStatement.() -> Unit,
    process: ResultSet.() -> T
): T {
    return query(sql, prepare) {
        first()
        process()
    }
}

inline fun <T : Any> DB.queryWithTotal(
    select: String,
    tableIsh: String,
    where: List<WhereClause>,
    order: String,
    limit: Int = Integer.MIN_VALUE,
    offset: Int = 0,
    process: ResultSet.() -> T
): ListResult<T> {
    val whereClause = where.toWhereStatementOrEmpty()

    //THIS IS BAD! Never concatenate SQL strings together, ever, ever, ever, ever!
    val total = queryAlwaysSingle("select count(*) from $tableIsh $whereClause", where.toPrepareFunc(), { getInt(1) })
    val paging = if (limit != Integer.MIN_VALUE) "limit $limit offset $offset" else ""

    val list = queryList("select $select from $tableIsh $whereClause order by $order $paging", where.toPrepareFunc(), process)
    return ListResult(list, total)
}

fun List<WhereClause>.toPrepareFunc(): PreparedStatement.() -> Unit {
    val clauses = this
    return {
        var idx = 1
        clauses.forEach {
            val setValues = it.setValues
            if (setValues != null) {
                idx += setValues(this, idx)
            }
        }
    }
}

data class WhereClause(
    val clause: String,
    val setValues: (PreparedStatement.(firstIndex: Int) -> Int)? = null
) {
    companion object {
        fun <T: Any> singleParam(
            clause: String,
            value: T,
            setValue: PreparedStatement.(index: Int, value: T) -> Unit
        ): WhereClause {
            return WhereClause(clause) { initialValue ->
                this.setValue(initialValue, value)
                1
            }
        }

        fun or(vararg clauses: WhereClause): WhereClause {
            return or(clauses.toList())
        }

        fun or(clauses: Collection<WhereClause>): WhereClause {
            if (clauses.isEmpty()) {
                throw IllegalArgumentException("Must specify at least one clause for 'OR'")
            }
            if (clauses.size == 1) {
                return clauses.first()
            }
            return WhereClause(
                clauses.joinToString(prefix = "(", separator = ") OR (", postfix = ")") { it.clause }
            ) { initialIndex ->
                var idx = 0
                clauses.forEach {
                    val set = it.setValues
                    if (set != null) {
                        this.set(initialIndex + idx++)
                    }
                }
               idx
            }
        }
    }
}

fun main(args: Array<String>) {
    val dir = Files.createTempDirectory("uapi-db-test")
    val db = DB(dir.resolve("library-db").toString())
    db.openConnection().use {
        it.prepareStatement("select count(*) from library.BOOK").executeQuery().use { rs ->
            rs.first()
            println(rs.getInt(1))
        }
    }
}
