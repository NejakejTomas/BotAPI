package dbs.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Inventories : Table() {
    val userId: Column<EntityID<Int>> = reference("userId", Players.userId, ReferenceOption.CASCADE)
}