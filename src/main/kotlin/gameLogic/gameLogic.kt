package gameLogic

import gameLogic.daily.DailyBonus
import gameLogic.daily.IDailyBonus

fun org.koin.core.module.Module.addGameLogic() {
    single<IDailyBonus> { DailyBonus() }
}