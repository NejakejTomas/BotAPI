package dbs.dao

import dbs.tables.DailyBonuses
import dbs.dataStructures.DailyStreak
import exceptions.OperationNotAllowedException
import exceptions.PlayerNotFoundException
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import text.Translatable
import java.time.LocalDate
import java.time.ZoneOffset

class DailyDao : IDailyDao {
    override fun getDailyStreak(playerId: Int): DailyStreak = transaction {
        val today = LocalDate.now(ZoneOffset.UTC)

        DailyBonuses
            .slice(DailyBonuses.streak, DailyBonuses.last)
            .select { DailyBonuses.userId eq playerId }
            .firstOrNull()
            ?.let {
                // Daily was today already claimed
                if (it[DailyBonuses.last] == today) DailyStreak(it[DailyBonuses.streak], true)
                // Daily was claimed yesterday but not today - we want number of consecutive days because it still can be claimed today and not broken
                else if (it[DailyBonuses.last] == today.minusDays(1)) DailyStreak(it[DailyBonuses.streak], false)
                // Daily was not claimed today or yesterday - streak is 0
                else DailyStreak(0, false)
            } ?: throw PlayerNotFoundException(playerId)
    }

    override fun getTodayDaily(playerId: Int): Unit = transaction {
        val lastDaily =(DailyBonuses
            .select { DailyBonuses.userId eq playerId }
            .firstOrNull() ?: throw PlayerNotFoundException(playerId))
            .let {
                Pair(it[DailyBonuses.streak], it[DailyBonuses.last])
            }

        val today = LocalDate.now()
        if (lastDaily.second == today) throw OperationNotAllowedException(Translatable("canNotClaimDailyBonus"), Translatable("itWasAlreadyClaimed"))
        val keepsStreak = lastDaily.second == today.minusDays(1)

        DailyBonuses
            .update({ DailyBonuses.userId eq playerId }) {
                with(SqlExpressionBuilder) {
                    it[DailyBonuses.last] = today

                    // Keep streak if yesterday was the last day daily bonus was claimed, but reset it if it was not
                    if (keepsStreak) it[DailyBonuses.streak] = DailyBonuses.streak + 1
                    else it[streak] = 1
                }
            }
    }
}