package dbs.dao

import dbs.dataStructures.Item
import dbs.tables.Items
import exceptions.ItemDoesNotExist
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class ItemDao : IItemDao {
    override fun getItem(itemId: Int): Item = transaction {
        Items
            .select { Items.id eq itemId }
            .firstOrNull()
            ?.let {
                Item(
                    it[Items.id].value,
                    it[Items.name],
                    it[Items.description]
                )
            } ?: throw ItemDoesNotExist(itemId)
    }

    override fun getItems(): List<Item> = transaction {
        Items
            .selectAll()
            .map {
                Item(
                    it[Items.id].value,
                    it[Items.name],
                    it[Items.description]
                )
            }
    }

    override fun createItem(name: String, description: String): Int = transaction {
        Items
            .insertAndGetId {
                it[Items.name] = name
                it[Items.description] = description
            }.value
    }

    override fun deleteItem(itemId: Int) = transaction {
        val deletedNumber = Items
            .deleteWhere { Items.id eq itemId }

        if (deletedNumber == 0) throw ItemDoesNotExist(itemId)
    }

    override fun modifyItem(itemId: Int, name: String?, description: String?) = transaction {
        val updatedNumber = Items
            .update ({ Items.id eq itemId }) {
                if(name != null) it[Items.name] = name
                if(description != null) it[Items.description] = description
            }

        if (updatedNumber == 0) throw ItemDoesNotExist(itemId)
    }
}