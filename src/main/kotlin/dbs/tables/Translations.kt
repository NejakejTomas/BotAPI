package dbs.tables

import dbs.TranslationKeyMaxLength
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

// TODO: Move languageCode to its own table
object Translations : Table() {
    val languageCode: Column<String> = varchar("languageCode", 10)
    val key: Column<String> = varchar("key", TranslationKeyMaxLength)
    val translation: Column<String> = text("translation")
    override val primaryKey = PrimaryKey(languageCode, key)
}