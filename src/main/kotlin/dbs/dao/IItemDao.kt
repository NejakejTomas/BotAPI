package dbs.dao

import dbs.dataStructures.Item

interface IItemDao {
    fun getItem(itemId: Int): Item
    fun getItems(): List<Item>
    fun createItem(name: String, description: String): Int
    fun deleteItem(itemId: Int)
    // Admin
    fun modifyItem(itemId: Int, name: String?, description: String?)
}