package dbs.dataStructures

data class Player(
    val id: Int,
    val money: Long,
    val experience: Long,
    val guildId: Int?
)