package dbs.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import java.time.LocalDate
import java.time.ZoneOffset

object DailyBonuses : Table() {
    val userId: Column<EntityID<Int>> = reference("userId", Players.userId, ReferenceOption.CASCADE)
    // Yesterday so player can claim first daily bonus on first day
    val last: Column<LocalDate> = date("last").default(LocalDate.now(ZoneOffset.UTC).minusDays(1))
    val streak: Column<Int> = integer("streak").default(0)
}