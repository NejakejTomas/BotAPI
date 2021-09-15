package gameLogic.daily

interface IDailyBonus {
    fun getToday(playerId: Int): AcquiredDaily
}