package rest.routes;

import com.google.gson.Gson
import dbs.dao.*
import dbs.dataStructures.Translation
import dbs.tables.*
import exceptions.HttpException
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.koin.dsl.module
import org.koin.core.component.KoinComponent
import org.koin.test.KoinTestRule
import kotlin.test.*

class TranslationTest : KoinComponent {
    private companion object : KoinComponent {
        @AfterClass
        @JvmStatic
        fun teardown() {
            TransactionManager.closeAndUnregister(database)
        }

        @BeforeClass
        @JvmStatic
        fun setup() {
            setupDatabase()
            setupDatabaseTables()
        }

        private lateinit var database: Database

        private fun setupDatabase() {
            database = Database.connect("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;", "org.h2.Driver", "root", "").apply {
                useNestedTransactions = true
            }
        }

        private val translationData = listOf(
            Triple("cs-cz", "StringToTranslate", "Řetězec k přeložení"),
            Triple("en-us", "StringToTranslate", "String to translate"),
            Triple("cs-cz", "AnotherStringToTranslate", "Další řetězec k přeložení"),
            Triple("en-us", "AnotherStringToTranslate", "Another string to translate"),
            Triple("cs-cz", "AnotherStringToTranslateNowWithNumber%sInIt", "Další řetězec k přeložení, tentokrát s číslem %s v něm"),
            Triple("en-us", "AnotherStringToTranslateNowWithNumber%sInIt", "Another string to translate, now with number %s in it"),
        )
        private val platformTranslationData = listOf(
        Triple("discord", "CoinImage", "<:coin:0123456789>"),
        Triple("web", "CoinImage", "https://example.com/coin.png"),
        Triple("discord", "BoldText", "**%s**"),
        Triple("web", "BoldText", "<b>%s</b>"),
        Triple("discord", "WithNewLine", "Some text\nrest of the text"),
        Triple("web", "WithNewLine", "Some text\r\nrest of the text"),
        )

        private fun setupDatabaseTables() {

            transaction {
                addLogger(StdOutSqlLogger)
                SchemaUtils.create(Translations)
                SchemaUtils.create(PlatformTranslations)

                translationData.forEach { data ->
                    Translations.insert {
                        it[Translations.languageCode] = data.first
                        it[Translations.key] = data.second
                        it[Translations.translation] = data.third
                    }
                }

                platformTranslationData.forEach { data ->
                    PlatformTranslations.insert {
                        it[PlatformTranslations.platform] = data.first
                        it[PlatformTranslations.key] = data.second
                        it[PlatformTranslations.translation] = data.third
                    }
                }
            }
        }
    }

    private val diModule = module {
        single<ITranslationDao> { TranslationDao() }
        single<IPlatformTranslationsDao> { PlatformTranslationsDao() }
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(diModule)
    }

    private fun io.ktor.application.Application.setupServer() {
        install(DefaultHeaders)
        install(ContentNegotiation) {
            register(Application.Json, GsonConverter())
        }
        install(StatusPages) {
            exception<HttpException> { cause ->
                run {
                    call.respond(cause.httpStatusCode, cause.message)
                }
            }
        }
        routing {
            TranslationRoute.accept(this)
        }
    }

    @Test
    fun testTranslationCz() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/translations/cS-cz")) {
                assertNotNull(response.content)
                val translations = Gson().fromJson(response.content, Array<Translation>::class.java)
                assert(translations.size >= 3)
                val original = translationData.fold(mutableSetOf<Translation>()) { container, item ->
                    if (item.first == "cs-cz") container.add(Translation(item.second, item.third))
                    container
                }

                assertEquals(original, translations.toSet())
            }
        }
    }

    @Test
    fun testTranslationEn() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/translations/en-US")) {
                assertNotNull(response.content)
                val translations = Gson().fromJson(response.content, Array<Translation>::class.java)
                assert(translations.size >= 3)
                val original = translationData.fold(mutableSetOf<Translation>()) { container, item ->
                    if (item.first == "en-us") container.add(Translation(item.second, item.third))
                    container
                }

                assertEquals(original, translations.toSet())
            }
        }
    }

    @Test
    fun testTranslationNotExists() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/translations/nothing-null")) {
                assertNotNull(response.content)
                val translations = Gson().fromJson(response.content, Array<Translation>::class.java)
                assertEquals(0, translations.size)
            }
        }
    }

    @Test
    fun testPlatformTranslationDiscord() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/platformTranslations/dISCorD")) {
                assertNotNull(response.content)
                val translations = Gson().fromJson(response.content, Array<Translation>::class.java)
                assert(translations.size >= 3)
                val original = platformTranslationData.fold(mutableSetOf<Translation>()) { container, item ->
                    if (item.first == "discord") container.add(Translation(item.second, item.third))
                    container
                }

                assertEquals(original, translations.toSet())
            }
        }
    }

    @Test
    fun testPlatformTranslationWeb() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/platformTranslations/WeB")) {
                assertNotNull(response.content)
                val translations = Gson().fromJson(response.content, Array<Translation>::class.java)
                assert(translations.size >= 3)
                val original = platformTranslationData.fold(mutableSetOf<Translation>()) { container, item ->
                    if (item.first == "web") container.add(Translation(item.second, item.third))
                    container
                }

                assertEquals(original, translations.toSet())
            }
        }
    }

    @Test
    fun testPlatformTranslationNotExists() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/platformTranslations/nothing")) {
                assertNotNull(response.content)
                val translations = Gson().fromJson(response.content, Array<Translation>::class.java)
                assertEquals(0, translations.size)
            }
        }
    }
}