package dbs.dao

import dbs.dataStructures.DailyStreak

interface IDailyDao {
    fun getDailyStreak(playerId: Int): DailyStreak
    fun getTodayDaily(playerId: Int)
}