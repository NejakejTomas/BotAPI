package rest.routes;

import com.google.gson.Gson
import dbs.dao.IPlayerDao
import dbs.dao.PlayerDao
import dbs.dataStructures.Player
import dbs.tables.*
import exceptions.HttpException
import gameLogic.IGameSettings
import io.ktor.application.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
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
import java.util.*
import kotlin.test.*

class PlayerTest : KoinComponent {
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
                single<IGameSettings> { TestGameSettings() }
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

        private lateinit var players: SortedSet<Int>

        private fun addPlayerData(): SortedSet<Int> {
            val dao: IPlayerDao by inject()
            return transaction {
                sortedSetOf(
                    dao.createPlayer(true, null),
                    dao.createPlayer(true, 10U),
                    dao.createPlayer(false, 11U),
                )
            }
        }
    }

    class TestGameSettings : IGameSettings {
        override val DailyMoney: Long = 100
        override val StartingMoney: Long = 50
    }

    private val diModule = module {
        single<IGameSettings> { TestGameSettings() }
        single<IPlayerDao> { PlayerDao() }
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
            PlayerRoute.accept(this)
        }
    }

    @Test
    fun testCreatePlayer() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Post, "/player"){
                addHeader(HttpHeaders.ContentType, Application.FormUrlEncoded.toString())
                setBody(listOf("dcSnowflake" to "42", "isAdmin" to "true").formUrlEncode())
            }) {
                assertNotNull(response.content?.toIntOrNull())
                Unit
            }
        }
    }

    @Test
    fun testCreatePlayerWithNullField() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Post, "/player"){
                addHeader(HttpHeaders.ContentType, Application.FormUrlEncoded.toString())
                setBody(listOf("isAdmin" to "false").formUrlEncode())
            }) {
                assertNotNull(response.content?.toIntOrNull())
                Unit
            }
        }
    }

    @Test
    fun testGetPlayer() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/player/${players.elementAt(0)}"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                val gameSettings: IGameSettings by inject()
                assertNotNull(response.content)
                val player: Player = Gson().fromJson(response.content, Player::class.java)
                assertNotNull(players.elementAt(0))

                assertEquals(0, player.experience)
                assertEquals(gameSettings.StartingMoney, player.money)

            }
        }
    }

    @Test
    fun testGetPlayerNotFound() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/player/${Int.MAX_VALUE}"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun testGetPlayerFailNotId() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/player/max"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testGetAllPlayers() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/player"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertNotNull(response.content)
                val players: List<Player> = Gson().fromJson(response.content, Array<Player>::class.java).toList()
                assertNotNull(players)
                assert(players.count() >=  3)
            }
        }
    }

    @Test
    fun testGetPlayerByDcSnowflake() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/player/byDcSnowflake/10"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                val gameSettings: IGameSettings by inject()
                val id = response.content?.toIntOrNull()
                assertNotNull(response.content)

                assertEquals(players.elementAt(1), id)
            }
        }
    }

    @Test
    fun testGetPlayerByDcSnowflakeFail() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/player/byDcSnowflake/1"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun testGetPlayerByDcSnowflakeFailNotSnowflake() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/player/byDcSnowflake/pepa"){
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }
}