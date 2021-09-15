package dbs.dao

import dbs.dataStructures.Translation
import dbs.tables.Translations
import exceptions.TranslationNotFoundException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class TranslationDao : ITranslationDao {
    override fun getTranslation(languageCode: String, translationString: String): String = transaction {
        Translations
            .select { (Translations.languageCode eq languageCode) and (Translations.key eq translationString) }
            .firstOrNull()
            ?.let {
                it[Translations.translation]
            }
            ?: throw TranslationNotFoundException(
                translationString,
                languageCode,
                !TranslationNotFoundException.forbiddenTranslations.contains(translationString)
            )
    }

    override fun getAllTranslations(languageCode: String): List<Translation> = transaction {
        Translations
            .select { Translations.languageCode eq languageCode }
            .map { Translation(it[Translations.key], it[Translations.translation]) }
    }
}