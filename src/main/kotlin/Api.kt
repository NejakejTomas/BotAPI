import dbs.dao.*
import dbs.tables.Players
import dbs.tables.Translations
import dbs.tables.Users
import dbs.tables.*
import exceptions.HttpException
import gameLogic.DummyGameSettings
import gameLogic.IGameSettings
import gameLogic.addGameLogic
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import rest.routes.*

object Api : KoinComponent {
    private fun setupDI() {
        val gameSettingsModule = module {
            single<IGameSettings> { DummyGameSettings() }
            single<IDailyDao> { DailyDao() }
            single<IInventoryDao> { InventoryDao() }
            single<IPlatformTranslationsDao> { PlatformTranslationsDao() }
            single<IPlayerDao> { PlayerDao() }
            single<ITranslationDao> { TranslationDao() }
            single<IItemDao> { ItemDao() }
            addGameLogic()
        }

        startKoin {
            modules(gameSettingsModule)
        }
    }

    private fun setupDatabase() {
        // TODO: TMP
        Database.connect("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;", "org.h2.Driver", "root", "").apply {
            useNestedTransactions = true
        }
    }

    private fun setupDatabaseTables() {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Inventories)
            SchemaUtils.create(Users)
            SchemaUtils.create(Players)
            SchemaUtils.create(Guilds)
            SchemaUtils.create(Items)
            SchemaUtils.create(InventoryItems)
            SchemaUtils.create(Translations)
            SchemaUtils.create(PlatformTranslations)
            SchemaUtils.create(DailyBonuses)
        }
    }

    private fun setupServer() {
        embeddedServer(Netty, port = 8080) {
            install(DefaultHeaders)
            install(ContentNegotiation) {
                register(ContentType.Application.Json, GsonConverter())
            }
            install(StatusPages) {
                exception<HttpException> { cause ->
                    run {
                        call.respond(cause.httpStatusCode, cause.message)
                    }
                }
            }
            routing {
                PlayerRoute.accept(this)
                DailyBonusRoute.accept(this)
                TranslationRoute.accept(this)
                ItemRoute.accept(this)
                InventoryRoute.accept(this)
            }
        }.start(wait = true)
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        setupDI()
        setupDatabase()
        if (args.find { it.lowercase().equals("--init-db") } != null) setupDatabaseTables()

        val dao: IInventoryDao by inject()


        setupServer()

        fun Transaction.translate(code: String, from: String, to: String) {
            transaction {
                Translations
                    .insert {
                        it[Translations.languageCode] = code
                        it[Translations.key] = from
                        it[Translations.translation] = to
                    }
            }
        }

        transaction {
            translate("cs-cz", "playerWithId", "hráč s ID")
            translate("cs-cz", "wasNotFound", "nebyl nalezen")
        }
    }
}
