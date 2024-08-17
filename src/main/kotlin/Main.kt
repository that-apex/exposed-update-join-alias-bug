package com.example

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun main() {
    Database.connect(
        "jdbc:mariadb://localhost:3306/test",
        user = "root",
        password = "verysecurepassword"
    )

    // prepare data
    println("setup")
    transaction {
        SchemaUtils.createMissingTablesAndColumns(TableBeingUpdated, TableBeingQueried)
        TableBeingQueried.deleteAll()
        TableBeingUpdated.deleteAll()

        TableBeingUpdated.insert {
            it[id] = 1
            it[valueToUpdate] = 42
        }

        TableBeingUpdated.insert {
            it[id] = 2
            it[valueToUpdate] = 400
        }

        TableBeingQueried.insert {
            it[id] = 1
            it[valueSource] = 12
            it[someState] = "WILL_UPDATE"
        }

        TableBeingQueried.insert {
            it[id] = 2
            it[valueSource] = 34
            it[someState] = "WILL_NOT_UPDATE"
        }
    }

    // bug
    println("bug")
    val subQuery = TableBeingQueried
        .selectAll()
        .where { TableBeingQueried.someState eq "WILL_UPDATE" } // Removing this line will make the update work (as there won't be any parameters in the subquery)
        .alias("sub_query")

    transaction {
        addLogger(StdOutSqlLogger)

        TableBeingUpdated
            .join(
                subQuery,
                joinType = JoinType.INNER,
                onColumn = TableBeingUpdated.id,
                otherColumn = subQuery[TableBeingQueried.id],
            )
            .update {
                it[TableBeingUpdated.valueToUpdate] = subQuery[TableBeingQueried.valueSource]
            }
    }
}

private object TableBeingUpdated : IntIdTable("table_being_updated") {
    val valueToUpdate = integer("value_to_update")
}

private object TableBeingQueried : Table("table_being_queried") {
    val id = reference("id", TableBeingUpdated)
    val valueSource = integer("value_source")
    val someState = varchar("some_state", 255)
}
