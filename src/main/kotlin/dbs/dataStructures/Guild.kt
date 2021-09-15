package dbs.dataStructures

data class Guild(
    val id: Int,
    val dcSnowflake: ULong,
    val name: String,
    val description: String,
    val playerCount: Int
)