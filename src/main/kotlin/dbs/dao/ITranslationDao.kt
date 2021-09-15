package dbs.dao

import dbs.dataStructures.Translation

interface ITranslationDao {
    fun getTranslation(languageCode: String, translationString: String): String
    fun getAllTranslations(languageCode: String): List<Translation>
}