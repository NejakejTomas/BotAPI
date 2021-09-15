package dbs.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object InventoryItems : Table() {
    val userId: Column<EntityID<Int>> = reference("userId", Inventories.userId, ReferenceOption.CASCADE)
    val itemId: Column<EntityID<Int>> = reference("itemId", Items, ReferenceOption.CASCADE)
    val count: Column<Int> = integer("count").default(0)
}