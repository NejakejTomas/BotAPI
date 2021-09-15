package dbs.dao

import dbs.dataStructures.InventoryItem
import dbs.dataStructures.Item
import dbs.tables.Inventories
import dbs.tables.InventoryItems
import dbs.tables.Items
import exceptions.ItemDoesNotExist
import exceptions.ItemNotInInventory
import exceptions.OperationNotAllowedException
import exceptions.PlayerNotFoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import text.Translatable

class InventoryDao : IInventoryDao {

    override fun getAllItems(playerId: Int): List<InventoryItem> = transaction {
        val itemsJoin = InventoryItems
            .join(Items, JoinType.INNER, InventoryItems.itemId, Items.id)

        // Left join, so we can tell if we have inventory, but not items
        val result = Inventories
            .join(itemsJoin, JoinType.LEFT, Inventories.userId, InventoryItems.userId)
            .slice(InventoryItems.count, Items.id, Items.name, Items.description)
            .select { Inventories.userId eq playerId }
            .map {
                // We got row where there is inventory, but not item, which means inventory exists, but it doesn't have
                // anything in it
                if (it[Items.id]._value == null) return@transaction listOf<InventoryItem>()

                InventoryItem(
                    it[Items.id].value,
                    it[Items.name],
                    it[Items.description],
                    it[InventoryItems.count],
                )
            }

        // If result is empty, that means there weren't any rows - not even inventory,
        // and because every player has to have one, that means player doesn't exist
        if (result.isEmpty()) throw PlayerNotFoundException(playerId)

        result
    }

    private fun ensureInventory(playerId: Int) {
        if (
            Inventories
                .select { Inventories.userId eq playerId }
                .count() == 0L
        ) throw PlayerNotFoundException(playerId)
    }

    private fun ensureItem(itemId: Int) {
        if (
            Items
                .select { Items.id eq itemId }
                .count() == 0L
        ) throw ItemDoesNotExist(itemId)
    }

    override fun getItem(playerId: Int, itemId: Int): InventoryItem = transaction {
        ensureInventory(playerId)
        ensureItem(itemId)

        val itemsJoin = InventoryItems
            .join(Items, JoinType.INNER, InventoryItems.itemId, Items.id)

        itemsJoin
            .join(Inventories, JoinType.INNER, InventoryItems.userId, Inventories.userId)
            .slice(InventoryItems.count, Items.id, Items.name, Items.description)
            .select { (Inventories.userId eq playerId) and (Items.id eq itemId) }
            .firstOrNull()
            ?.let {
                InventoryItem(
                    it[Items.id].value,
                    it[Items.name],
                    it[Items.description],
                    it[InventoryItems.count],
                )
            } ?: throw ItemNotInInventory(itemId, playerId)
    }

    override fun modifyItem(playerId: Int, itemId: Int, addCount: Int) = transaction {
        if (addCount == 0) return@transaction

        ensureInventory(playerId)
        ensureItem(itemId)

        // Check, if when removing items, it will not result in less than 0 items in inventory
        if (addCount < 0) {
            InventoryItems
                .slice(InventoryItems.count)
                .select { (InventoryItems.userId eq playerId) and (InventoryItems.itemId eq itemId) }
                .firstOrNull()
                ?.let {
                    if (it[InventoryItems.count] + addCount == 0) {
                        // Do not just set count to 0, delete it form inventory completely
                        InventoryItems
                            .deleteWhere { (InventoryItems.userId eq playerId) and (InventoryItems.itemId eq itemId) }

                        return@transaction
                    }
                    if (it[InventoryItems.count] + addCount < 0)
                        throw OperationNotAllowedException(Translatable("modifyItem"), Translatable("badInput"))
                } ?: throw ItemNotInInventory(itemId, playerId)
        }



        val updatedNumber = InventoryItems
            .update ({ (InventoryItems.userId eq playerId) and (InventoryItems.itemId eq itemId) }) {
                with (SqlExpressionBuilder) {
                    it[InventoryItems.count] = InventoryItems.count + addCount
                }
        }

        // Item is not in inventory (and we are adding it), add it
        if (updatedNumber == 0) {
            InventoryItems.insert {
                it[InventoryItems.itemId] = itemId
                it[InventoryItems.userId] = playerId
                it[InventoryItems.count] = addCount
            }
        }
    }
}