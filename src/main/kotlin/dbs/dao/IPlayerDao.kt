package dbs.dao

import dbs.dataStructures.Player

interface IPlayerDao {
    fun createPlayer(isAdmin: Boolean, dcSnowflake: ULong?): Int
    fun getAllPlayers(): List<Player>
    fun getPlayer(playerId: Int): Player
    fun getPlayerIdByDcSnowflake(dcSnowflake: ULong): Int
    fun getMoney(playerId: Int): Long
    fun setMoney(playerId: Int, value: Long)
    fun addMoney(playerId: Int, value: Long)
    fun getGuildId(playerId: Int): Int?
    fun setGuildId(playerId: Int, guildId: Int?)
    fun getExperience(playerId: Int): Long
    fun setExperience(playerId: Int, value: Long)
    fun addExperience(playerId: Int, value: Long)
}