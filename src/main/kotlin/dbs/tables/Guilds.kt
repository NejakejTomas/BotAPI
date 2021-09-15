package dbs.tables

import dbs.TranslationKeyMaxLength
import dbs.tables.Users.default
import dbs.tables.Users.nullable
import dbs.tables.Users.uniqueIndex
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Guilds : IntIdTable() {
    // TODO: Move dcSnowflake to its own table
    val dcSnowflake: Column<ULong?> = ulong("dcSnowflake").nullable().uniqueIndex().default(null)
    val name: Column<String> = varchar("name", TranslationKeyMaxLength)
    val description: Column<String> = varchar("description", TranslationKeyMaxLength)
}