package dbs.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime
import java.time.ZoneOffset

object Users : IntIdTable() {
    val dateJoined: Column<LocalDateTime> = datetime("dateJoined").default(LocalDateTime.now(ZoneOffset.UTC))
    val isAdmin: Column<Boolean> = bool("isAdmin")
    // TODO: Move dcSnowflake to its own table
    // Unique but due to database differences (with nullable uniqueness) not marked as one
    val dcSnowflake: Column<ULong?> = ulong("dcSnowflake").nullable().uniqueIndex().default(null)
}