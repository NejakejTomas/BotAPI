package dbs.dao

import dbs.dataStructures.Translation

interface IPlatformTranslationsDao {
    fun getPlatformTranslation(platform: String, platformTranslationString: String): String
    fun getAllPlatformTranslations(platform: String): List<Translation>
}