package rest.routes

import dbs.dao.IPlayerDao
import exceptions.BadRequestException
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import dbs.dataStructures.Player
import exceptions.PlayerNotFoundException
import text.Literal

object PlayerRoute : KoinComponent, IInstallable {
    private val dao: IPlayerDao by inject()

    override fun accept(routing: Route) {
        routing.route("player") {
            createPlayer()
            getAllPlayers()
            getPlayerIdByDcSnowflake()
        }

        routing.route("player/{playerId}") {
            getPlayer()
            DailyBonusRoute.accept(this)
        }
    }

    /**
     * Returns id of newly created player
     *
     * Accepts optional `isAdmin` if player should be marked as admin, false if not preset,
     * optional `dcSnowflake` with Discord snowflake which can be used for identifying the player.
     * Returns [BadRequestException] if `isAdmin` or `dcSnowflake` is malformed
     */
    private fun Route.createPlayer() {
        post("") {
            val parameters = call.receiveParameters()

            val isAdmin = if (parameters["isAdmin"] == null) false
            else (parameters["isAdmin"]?.toBooleanStrictOrNull() ?: throw BadRequestException(Literal("Malformed 'isAdmin'")))
            //val isAdmin = parameters["isAdmin"]?.toBooleanStrictOrNull() ?: throw BadRequestException()
            val dcSnowflake = if (parameters["dcSnowflake"] == null) null as ULong?
            else (parameters["dcSnowflake"]?.toULongOrNull() ?: throw BadRequestException(Literal("Malformed 'dcSnowflake'")))


            call.respondText(dao.createPlayer(isAdmin, dcSnowflake).toString())
        }
    }

    /**
     * Returns all players as collection of [Player]s
     *
     * **Not meant for normal game use**
     */
    private fun Route.getAllPlayers() {
        get("") {
            call.respond(dao.getAllPlayers())
        }
    }

    /**
     * Returns player as [Player]
     *
     * Accepts `playerId` with player id, returns [BadRequestException] when malformed, or [PlayerNotFoundException]
     * when player with that id does not exist
     */
    private fun Route.getPlayer() {
        get("") {
            val id = call.parameters["playerId"]?.toIntOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'playerId'"))

            val player = dao.getPlayer(id)

            call.respond(player)
        }
    }

    /**
     * Returns player as [Player]
     *
     * Accepts `dcSnowflake` with Discord snowflake, returns [BadRequestException] when malformed,
     * or [PlayerNotFoundException] when player with that snowflake does not exist
     */
    private fun Route.getPlayerIdByDcSnowflake() {
        get("byDcSnowflake/{dcSnowflake}") {
            val id = call.parameters["dcSnowflake"]?.toULongOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'dcSnowflake'"))

            val player = dao.getPlayerIdByDcSnowflake(id)

            call.respondText(player.toString())
        }
    }
}