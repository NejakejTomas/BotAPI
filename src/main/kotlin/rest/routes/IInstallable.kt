package rest.routes

import io.ktor.routing.*

interface IInstallable {
    fun accept(routing: Route)
}