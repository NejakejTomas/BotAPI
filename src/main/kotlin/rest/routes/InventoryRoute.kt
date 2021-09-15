package rest.routes

import dbs.dao.IInventoryDao
import exceptions.BadRequestException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import text.Literal

object InventoryRoute : KoinComponent, IInstallable {
    private val dao: IInventoryDao by inject()

    override fun accept(routing: Route) {
        routing.route("inventory/{playerId}") {
            getAllItems()
            getItem()
            modifyItem()
        }
    }

    private fun Route.getAllItems() {
        get("") {
            val playerId = call.parameters["playerId"]?.toIntOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'playerId'"))

            val items = dao.getAllItems(playerId)

            call.respond(items)
        }
    }

    private fun Route.getItem() {
        get("/{itemId}") {
            val playerId = call.parameters["playerId"]?.toIntOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'playerId'"))
            val itemId = call.parameters["itemId"]?.toIntOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'itemId'"))

            val item = dao.getItem(playerId, itemId)

            call.respond(item)
        }
    }

    private fun Route.modifyItem() {
        post("/{itemId}") {
            val playerId = call.parameters["playerId"]?.toIntOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'playerId'"))
            val itemId = call.parameters["itemId"]?.toIntOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'itemId'"))

            val parameters = call.receiveParameters()
            val addCount = parameters["addCount"]?.toIntOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'addCount'"))

            dao.modifyItem(playerId, itemId, addCount)

            call.respond(HttpStatusCode.OK)
        }
    }
}