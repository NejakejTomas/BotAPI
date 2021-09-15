package dbs.dataStructures

import java.time.LocalDateTime

data class User(
    val id: Int,
    val dateJoined: LocalDateTime,
    val isAdmin: Boolean,
    val dcSnowflake: ULong?
)