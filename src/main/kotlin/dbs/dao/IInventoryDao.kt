package dbs.dao

import dbs.dataStructures.InventoryItem

interface IInventoryDao {
    fun getAllItems(playerId: Int): List<InventoryItem>
    fun getItem(playerId: Int, itemId: Int): InventoryItem
    fun modifyItem(playerId: Int, itemId: Int, addCount: Int)
}