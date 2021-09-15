package rest.routes

import dbs.dao.IPlatformTranslationsDao
import dbs.dao.ITranslationDao
import dbs.dataStructures.Translation
import exceptions.BadRequestException
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import text.Literal

object TranslationRoute : KoinComponent, IInstallable {
    private val dao = object : ITranslationDao by get(), IPlatformTranslationsDao by get() { }

    override fun accept(routing: Route) {
        routing.route("translations") {
            getAllTranslations()
        }

        routing.route("platformTranslations") {
            getAllPlatformTranslations()
        }
    }

    /**
     * Returns collection of [Translation]s
     *
     * Accepts `languageCode` with language code of wanted language, returns empty collection if
     * that language does not exist or [BadRequestException] if it is somehow else malformed
     */
    private fun Route.getAllTranslations() {
        get("{languageCode}") {
            val languageCode = call.parameters["languageCode"] ?: throw BadRequestException(Literal("Missing / malformed 'languageCode'"))
            call.respond(dao.getAllTranslations(languageCode.lowercase()))
        }
    }

    /**
     * Returns collection of platform [Translation]s
     *
     * Accepts `platform` with desired platform, returns empty collection if
     * that platform does not exist or [BadRequestException] if it is somehow else malformed
     *
     * Meant for example if we have money icon and we want to whow it inside of text. On for example Discord, we
     * have to show it as emote (`<:coin:0123456789>` for example), and on web we want to create `<img>` tag with `src`
     * to some url with coin image, so we want that url
     */
    private fun Route.getAllPlatformTranslations() {
        get("{platform}") {
            val platform = call.parameters["platform"] ?: throw BadRequestException(Literal("Missing / malformed 'platform'"))
            call.respond(dao.getAllPlatformTranslations(platform.lowercase()))
        }
    }
}