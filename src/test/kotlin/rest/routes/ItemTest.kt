package rest.routes;

import com.google.gson.Gson
import dbs.dao.*
import dbs.dataStructures.Item
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

class ItemTest : KoinComponent {
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

        private val items = mutableListOf(
            "superSword" to "extraDuperSuperSword",
            "helmet" to "regularHelmet",
            "pocketWoollyMammoth" to "smolWoollyMammothToPetIfYouHaveABadDay",
        )

        private lateinit var itemIds: MutableList<Int>

        private fun setupDatabaseTables() {

            transaction {
                addLogger(StdOutSqlLogger)
                SchemaUtils.create(Items)

                val ids = mutableListOf<Int>()

                items.forEach { data ->
                    ids.add(Items.insertAndGetId {
                        it[Items.name] = data.first
                        it[Items.description] = data.second
                    }.value)
                }

                itemIds = ids
            }
        }
    }

    private val diModule = module {
        single<IItemDao> { ItemDao() }
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
            ItemRoute.accept(this)
        }
    }

    @Test
    fun getAllItems() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/item") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertNotNull(response.content)
                val items = Gson().fromJson(response.content, Array<Item>::class.java)
                assert(items.size >= 3)

                ItemTest.items.forEach {
                    assertNotNull(items.find { item ->
                        (it.first == item.name) && (it.second == item.description)
                    })
                }
            }
        }
    }

    @Test
    fun getItem() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/item/${itemIds.first()}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertNotNull(response.content)
                val item = Gson().fromJson(response.content, Item::class.java)
                assertEquals(itemIds.first(), item.id)
                assertEquals(items.first().first, item.name)
                assertEquals(items.first().second, item.description)
            }
        }
    }

    @Test
    fun getItemNotFound() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Get, "/item/${Int.MAX_VALUE}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun deleteItem() {
        return withTestApplication({ setupServer() }) {
            with(handleRequest(HttpMethod.Delete, "/item/${itemIds.first()}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
            }) {
                // Check return value
                assertEquals(HttpStatusCode.OK, response.status())

                // Check item was deleted
                with(handleRequest(HttpMethod.Get, "/item/${itemIds.first()}") {
                    addHeader(HttpHeaders.Accept, Application.Json.toString())
                }) {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }

                // Add it back
                transaction {
                    itemIds.set(0, Items.insertAndGetId {
                        it[Items.name] = items.first().first
                        it[Items.description] = items.first().second
                    }.value)
                }

                // Check it was added back
                with(handleRequest(HttpMethod.Get, "/item/${itemIds.first()}") {
                    addHeader(HttpHeaders.Accept, Application.Json.toString())
                }) {
                    assertNotNull(response.content)
                    val item = Gson().fromJson(response.content, Item::class.java)
                    assertEquals(itemIds.first(), item.id)
                    assertEquals(items.first().first, item.name)
                    assertEquals(items.first().second, item.description)
                }
            }
        }
    }

    @Test
    fun modifyItem() {
        return withTestApplication({ setupServer() }) {
                with(handleRequest(HttpMethod.Post, "/item/${itemIds.first()}") {
                addHeader(HttpHeaders.Accept, Application.Json.toString())
                addHeader(HttpHeaders.ContentType, Application.FormUrlEncoded.toString())
                items.set(0, items.first().first to "aaa")
                setBody(listOf("description" to items.first().second).formUrlEncode())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())

                // Check it was changed
                with(handleRequest(HttpMethod.Get, "/item/${itemIds.first()}") {
                    addHeader(HttpHeaders.Accept, Application.Json.toString())
                }) {
                    assertNotNull(response.content)
                    val item = Gson().fromJson(response.content, Item::class.java)
                    assertEquals(itemIds.first(), item.id)
                    assertEquals(items.first().first, item.name)
                    assertEquals(items.first().second, item.description)
                }
            }
        }
    }
}