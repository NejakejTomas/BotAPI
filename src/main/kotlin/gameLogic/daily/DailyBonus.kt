package gameLogic.daily

import dbs.dao.IDailyDao
import dbs.dao.IPlayerDao
import gameLogic.IGameSettings
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class DailyBonus : IDailyBonus, KoinComponent {
    private val dao = object : IPlayerDao by get(), IDailyDao by get() { }
    private val gameSettings: IGameSettings by inject()

    override fun getToday(playerId: Int): AcquiredDaily = transaction {
        try {
            val playerMoney = dao.getMoney(playerId)
            // Daily was already claimed today
            if (dao.getDailyStreak(playerId).claimedToday) return@transaction AcquiredDaily(0, playerMoney)

            dao.getTodayDaily(playerId)

            val newMoney = playerMoney + gameSettings.DailyMoney
            dao.setMoney(playerId, newMoney)

            return@transaction AcquiredDaily(gameSettings.DailyMoney, newMoney)
        } catch (e: Exception) {
            // Some bad thing has happened - rollback any change we made
            rollback()
            throw e
        }
    }
}
