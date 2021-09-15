package dbs.dao

import dbs.dataStructures.Translation
import dbs.tables.PlatformTranslations
import exceptions.TranslationNotFoundException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class PlatformTranslationsDao : IPlatformTranslationsDao {
    override fun getPlatformTranslation(platform: String, platformTranslationString: String): String = transaction {
        PlatformTranslations
            .select { (PlatformTranslations.platform eq platform) and (PlatformTranslations.key eq platformTranslationString) }
            .firstOrNull()
            ?.let {
                it[PlatformTranslations.translation]
            }
            ?: throw TranslationNotFoundException(platformTranslationString, platform)
    }

    override fun getAllPlatformTranslations(platform: String): List<Translation> = transaction {
        PlatformTranslations
            .select { PlatformTranslations.platform eq platform }
            .map { Translation(it[PlatformTranslations.key], it[PlatformTranslations.translation]) }
    }
}