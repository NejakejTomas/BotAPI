package dbs.tables

import dbs.TranslationKeyMaxLength
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Items : IntIdTable() {
    val name: Column<String> = varchar("name", TranslationKeyMaxLength)
    val description: Column<String> = varchar("description", TranslationKeyMaxLength)
}