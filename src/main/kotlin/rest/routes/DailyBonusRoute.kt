package rest.routes

import exceptions.BadRequestException
import exceptions.PlayerNotFoundException
import gameLogic.daily.IDailyBonus
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import gameLogic.daily.AcquiredDaily
import text.Literal

object DailyBonusRoute : KoinComponent, IInstallable {
    private val dailyBonus: IDailyBonus by inject()

    override fun accept(routing: Route) {
        routing.route("daily") {
            getDaily()
        }
    }

    /**
     * Acquires daily bonus for player as [AcquiredDaily]
     *
     * Accepts `playerId` with player id, returns [AcquiredDaily] with money acquired
     * (0 when tried to claim for the second time that day), [PlayerNotFoundException]
     * when player with that id does not exist, or [BadRequestException] when id is malformed
     */
    private fun Route.getDaily() {
        post("") {
            val parameters = call.receiveParameters()
            val playerId = parameters["playerId"]?.toIntOrNull() ?: throw BadRequestException(Literal("Missing / malformed 'playerId'"))

            call.respond(dailyBonus.getToday(playerId))
        }
    }
}