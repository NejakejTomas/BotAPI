package rest.routes

import dbs.dao.IItemDao
import dbs.dataStructures.Item
import exceptions.BadRequestException
import exceptions.ItemDoesNotExist
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import text.Literal

object ItemRoute : KoinComponent, IInstallable {
    private val dao: IItemDao by inject()

    override fun accept(routing: Route) {
        routing.route("item") {
            createItem()
            getAllItems()
        }
        routing.route("item/{itemId}") {
            getItem()
            deleteItem()
            modifyItem()
        }
    }

    /**
     * Returns id of newly created item
     *
     * Accepts `name` with key to translation of item name,
     * and `description` with description to translation of item description
     * Returns [BadRequestException] if `name` or `description` is missing
     */
    private fun Route.createItem() {
        post("") {
            val parameters = call.receiveParameters()

            val name = parameters["name"] ?: throw BadRequestException(Literal("Missing / malformed 'name'"))
            val description = parameters["description"] ?: throw BadRequestException(Literal("Missing / malformed 'description'"))

            call.respondText(dao.createItem(name, description).toString())
        }
    }

    /**
     * Returns item with id `itemId`
     *
     * Accepts `itemId` with id of item to delete
     * Returns [ItemDoesNotExist] if `itemId` is malformed, or [ItemDoesNotExist] if item with `itemId` does not exist
     */
    private fun Route.getItem() {
        get("") {
            val itemId = call.parameters["itemId"]?.toIntOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'itemId'"))

            call.respond(dao.getItem(itemId))
        }
    }

    /**
     * Returns collection of all [Item]s
     */
    private fun Route.getAllItems() {
        get("") {
            call.respond(dao.getItems())
        }
    }

    /**
     * Deletes item
     *
     * Accepts `itemId` with id of item to delete
     * Returns [ItemDoesNotExist] if `itemId` is malformed, or [ItemDoesNotExist] if item with `itemId` does not exist
     */
    private fun Route.deleteItem() {
        delete("") {
            val itemId = call.parameters["itemId"]?.toIntOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'itemId'"))
            dao.deleteItem(itemId)
            call.respond(HttpStatusCode.OK)
        }
    }

    /**
     * Modifies item
     *
     * Accepts `itemId` with item id to modify, optional `name` with new key to translation of item name,
     * and optional `description` with new description to translation of item description
     * Returns [ItemDoesNotExist] if `itemId` is malformed, or [ItemDoesNotExist] if item with `itemId` does not exist
     */
    private fun Route.modifyItem() {
        post("") {
            val parameters = call.receiveParameters()

            val itemId = call.parameters["itemId"]?.toInt() ?: throw BadRequestException(Literal("Missing / malformed 'itemId'"))
            val name = parameters["name"]
            val description = parameters["description"]

            dao.modifyItem(itemId, name, description)

            call.respond(HttpStatusCode.OK)
        }
    }
}