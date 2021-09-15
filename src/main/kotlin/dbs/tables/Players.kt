package dbs.tables

import gameLogic.IGameSettings
import org.jetbrains.exposed.dao.id.*
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object Players : Table(), KoinComponent {
    private val settings: IGameSettings by inject()
    val userId: Column<EntityID<Int>> = reference("userId", Users, ReferenceOption.CASCADE)
    val money: Column<Long> = long("money").default(settings.StartingMoney)
    val experience: Column<Long> = long("experience").default(0)
    val guildId: Column<EntityID<Int>?> = optReference("guildId", Guilds, ReferenceOption.SET_NULL).default(null)
}