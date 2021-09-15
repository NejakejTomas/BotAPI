package exceptions

import text.Text
import text.Literal
import text.Translatable

class TranslationNotFoundException private constructor(message: Text) : NotFoundException(message) {
    constructor(translationString: String, translationCode: String, doTranslate: Boolean = true)
            : this(
        if (doTranslate) Translatable(translationKey,
            Literal(translationString),
            Literal(translationCode)
        )
        else Literal("Translation for string %s with translation code %s was not found",
        Literal(translationString),
        Literal(translationCode)
        )
    )

    companion object {
        private const val translationKey = "TranslationForString%sWithTranslationCode%sWasNotFound"
        // Translations used by these exceptions - in some situations we do not want to translate them because
        // they could result in infinite loop (translation does not exist -> throw exception with that translation key
        // -> call API for that key -> translation does not exist -> ...)
        val forbiddenTranslations = setOf(translationKey)
    }
}