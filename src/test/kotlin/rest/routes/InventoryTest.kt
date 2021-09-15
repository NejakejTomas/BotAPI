package rest.routes;

import com.google.gson.Gson
import dbs.dao.*
import dbs.dataStructures.Inventory
import dbs.dataStructures.InventoryItem
import dbs.dataStructures.Item
import dbs.tables.*
import exceptions.HttpException
import gameLogic.IGameSettings
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
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext
import org.koin.test.KoinTestRule
import kotlin.properties.Delegates
import kotlin.test.*

class InventoryTest : KoinComponent {
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
            GlobalContext.stopKoin()
        }

        private fun setupDI() {
            val testModule = module {
                single<IGameSettings> { PlayerTest.TestGameSettings() }
                single<IInventoryDao> { InventoryDao() }
                single<IPlayerDao> { PlayerDao() }
                single<IItemDao> { ItemDao() }
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

        private val items = mutableListOf(
            "superSword" to "extraDuperSuperSword",
            "helmet" to "regularHelmet",
            "pocketWoollyMammoth" to "smolWoollyMammothToPetIfYouHaveABadDay",
        )

        private lateinit var itemIds: MutableList<Int>
        private var playerId: Int = -1

        private fun setupDatabaseTables() {
            val dao = object : IPlayerDao by get(), IInventoryDao by get(), IItemDao by get() { }

            transaction {
                addLogger(StdOutSqlLogger)
                SchemaUtils.create(Inventories)
                SchemaUtils.create(InventoryItems)
                SchemaUtils.create(DailyBonuses)
                SchemaUtils.create(Users)
                SchemaUtils.create(Players)
                SchemaUtils.create(Items)



                val ids = mutableListOf<Int>()

                items.forEach { ids.add(dao.createItem(it.first, it.second)) }
                itemIds = ids

                playerId = dao.createPlayer(false, null)

                dao.modifyItem(playerId, itemIds[0], 1)
                dao.modifyItem(playerId, itemIds[1], 10)
            }
        }
    }

    private val diModule = module {
        single<IInventoryDao> { InventoryDao() }
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
            InventoryRoute.accept(this)
        }
    }

    @Test
    fun getItem() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/inventory/${playerId}/${itemIds.first()}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertNotNull(response.content)
                val item = Gson().fromJson(response.content, InventoryItem::class.java)
                assertEquals(items.first().first, item.name)
                assertEquals(items.first().second, item.description)
            }
        }
    }

    @Test
    fun getItemItemNotfound() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/inventory/${playerId}/${Int.MAX_VALUE}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun getItemPlayerNotfound() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/inventory/${Int.MAX_VALUE}/${itemIds.first()}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun getAllItems() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/inventory/${playerId}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertNotNull(response.content)
                val items = Gson().fromJson(response.content, Array<InventoryItem>::class.java)
                assert(items.size >= 2)

                items.forEach {
                    assertNotNull(InventoryTest.items.find { item ->
                        (item.first == it.name) && (item.second == it.description)
                    })
                }
            }
        }
    }

    @Test
    fun addItem() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Post, "/inventory/${playerId}/${itemIds.last()}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
                addHeader(HttpHeaders.ContentType, Application.FormUrlEncoded.toString())
                setBody(listOf("addCount" to 3.toString()).formUrlEncode())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            // Check if it was added
            with(handleRequest(HttpMethod.Get, "/inventory/${playerId}/${itemIds.last()}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertNotNull(response.content)
                val item = Gson().fromJson(response.content, InventoryItem::class.java)

                assertEquals(items[2].first, item.name)
                assertEquals(items[2].second, item.description)
                assertEquals(3, item.count)
            }

            // Remove it again
            with(handleRequest(HttpMethod.Post, "/inventory/${playerId}/${itemIds.last()}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
                addHeader(HttpHeaders.ContentType, Application.FormUrlEncoded.toString())
                setBody(listOf("addCount" to (-3).toString()).formUrlEncode())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            // Check if it is gone
            with(handleRequest(HttpMethod.Get, "/inventory/${playerId}/${itemIds.last()}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
}