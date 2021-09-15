package dbs.tables

import dbs.PlatformTranslationMaxLength
import dbs.TranslationKeyMaxLength
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

// TODO: Move platform to its own table
object PlatformTranslations : Table() {
    val platform: Column<String> = varchar("platform", 10)
    val key: Column<String> = varchar("key", TranslationKeyMaxLength)
    val translation: Column<String> = varchar("translation", PlatformTranslationMaxLength)
    override val primaryKey = PrimaryKey(platform, key)
}