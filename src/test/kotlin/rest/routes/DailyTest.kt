package rest.routes;

import com.google.gson.Gson
import dbs.dao.DailyDao
import dbs.dao.IDailyDao
import dbs.dao.IPlayerDao
import dbs.dao.PlayerDao
import dbs.tables.*
import exceptions.HttpException
import gameLogic.IGameSettings
import gameLogic.daily.AcquiredDaily
import gameLogic.daily.DailyBonus
import gameLogic.daily.IDailyBonus
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
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.test.KoinTestRule
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.*

class DailyTest : KoinComponent {
    private companion object : KoinComponent {
        @AfterClass
        @JvmStatic
        fun teardown() {
            TransactionManager.closeAndUnregister(database)
        }

        @BeforeClass
        @JvmStatic
        fun setup() {
            setupDI()
            setupDatabase()
            setupDatabaseTables()
            players = addPlayerData()
            GlobalContext.stopKoin()
        }

        private fun setupDI() {
            val testModule = module {
                single<IGameSettings> { PlayerTest.TestGameSettings() }
                single<IPlayerDao> { PlayerDao() }
            }

            GlobalContext.startKoin {
                modules(testModule)
            }
        }

        private lateinit var database: Database

        private fun setupDatabase() {
            database = Database.connect("jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;", "org.h2.Driver", "root", "").apply {
                useNestedTransactions = true
            }
        }

        private fun setupDatabaseTables() {
            transaction {
                addLogger(StdOutSqlLogger)
                SchemaUtils.create(Inventories)
                SchemaUtils.create(Users)
                SchemaUtils.create(Players)
                SchemaUtils.create(Items)
                SchemaUtils.create(DailyBonuses)
            }
        }

        private lateinit var players: List<Int>

        private fun addPlayerData(): List<Int> {
            val dao: IPlayerDao by inject()
            return transaction {
                val ids = listOf(
                    dao.createPlayer(true, null),
                    dao.createPlayer(false, 12U),
                )

                // Set daily bonus of first player to today, so he can't claim it
                DailyBonuses
                    .update ({ DailyBonuses.userId eq ids.first() }, 1) {
                        with(SqlExpressionBuilder) {
                            it[DailyBonuses.last] = LocalDate.now(ZoneOffset.UTC)
                        }
                    }

                ids
            }
        }
    }

    class TestGameSettings : IGameSettings {
        override val DailyMoney: Long = 100
        override val StartingMoney: Long = 50
    }

    private val diModule = module {
        single<IGameSettings> { TestGameSettings() }
        single<IDailyBonus> { DailyBonus() }
        single<IPlayerDao> { PlayerDao() }
        single<IDailyDao> { DailyDao() }
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
            DailyBonusRoute.accept(this)
        }
    }

    @Test
    fun testAlreadyClaimed() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Post, "/daily"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
                addHeader(HttpHeaders.ContentType, Application.FormUrlEncoded.toString())
                setBody(listOf("playerId" to players[0].toString()).formUrlEncode())
            }) {
                val gameSettings: IGameSettings by inject()
                assertNotNull(response.content)
                val daily = Gson().fromJson(response.content, AcquiredDaily::class.java)
                assertEquals(0, daily.dailyAcquired)
                assertEquals(gameSettings.StartingMoney, daily.newMoneyBalance)
            }
        }
    }

    @Test
    fun testPlayerNotFound() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Post, "/daily"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
                addHeader(HttpHeaders.ContentType, Application.FormUrlEncoded.toString())
                setBody(listOf("playerId" to Int.MAX_VALUE.toString()).formUrlEncode())
            }) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun testPlayerNotId() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Post, "/daily"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
                addHeader(HttpHeaders.ContentType, Application.FormUrlEncoded.toString())
                setBody(listOf("playerId" to "franta").formUrlEncode())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testSuccess() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Post, "/daily"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
                addHeader(HttpHeaders.ContentType, Application.FormUrlEncoded.toString())
                setBody(listOf("playerId" to players[1].toString()).formUrlEncode())
            }) {
                val gameSettings: IGameSettings by inject()
                assertNotNull(response.content)
                val daily = Gson().fromJson(response.content, AcquiredDaily::class.java)
                assertEquals(gameSettings.DailyMoney, daily.dailyAcquired)
                assertEquals(gameSettings.StartingMoney + gameSettings.DailyMoney, daily.newMoneyBalance)
            }
        }
    }
}