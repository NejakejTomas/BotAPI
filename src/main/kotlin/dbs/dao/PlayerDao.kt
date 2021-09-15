package dbs.dao

import dbs.dataStructures.Player
import dbs.tables.*
import exceptions.DcPlayerNotFoundException
import exceptions.GuildNotFoundException
import exceptions.PlayerNotFoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class PlayerDao : IPlayerDao {
    override fun createPlayer(isAdmin: Boolean, dcSnowflake: ULong?): Int = transaction {
        // Exposed doesn't support triggers - we need to do it manually
        val id = Users
            .insertAndGetId {
                it[Users.isAdmin] = isAdmin
                it[Users.dcSnowflake] = dcSnowflake
            }.value

        Players
            .insert { it[Players.userId] = id }

        Inventories
            .insert { it[Inventories.userId] = id }

        DailyBonuses
            .insert { it[DailyBonuses.userId] = id }

        id
    }

    override fun getAllPlayers(): List<Player> = transaction {
        Players
            .slice(Players.userId, Players.money, Players.experience, Players.guildId)
            .selectAll()
            .map {
                Player(
                    it[Players.userId].value,
                    it[Players.money],
                    it[Players.experience],
                    it[Players.guildId]?.value,
                )
            }
    }

    override fun getPlayer(playerId: Int): Player = transaction {
        Players
            .slice(Players.userId, Players.money, Players.experience, Players.guildId)
            .select { Players.userId eq playerId }
            .firstOrNull()
            ?.let {
                Player(
                    it[Players.userId].value,
                    it[Players.money],
                    it[Players.experience],
                    it[Players.guildId]?.value,
                )
            } ?: throw PlayerNotFoundException(playerId)
    }

    override fun getPlayerIdByDcSnowflake(dcSnowflake: ULong): Int = transaction {
        return@transaction Users.join(Players, JoinType.INNER, Players.userId, Users.id)
            .slice(Players.userId)
            .select { Users.dcSnowflake eq dcSnowflake }
            .firstOrNull()
            ?.let {
                it[Players.userId].value
            } ?: throw DcPlayerNotFoundException(dcSnowflake)
    }

    override fun getMoney(playerId: Int): Long = transaction {
        Players
            .slice(Players.money)
            .select { Players.userId eq playerId }
            .firstOrNull()?.let {
                it[Players.money]
            } ?: throw PlayerNotFoundException(playerId)
    }

    override fun setMoney(playerId: Int, value: Long): Unit = transaction {
        val updatedCount = Players
            .update({ Players.userId eq playerId }, 1) {
                it[Players.money] = value
            }

        if (updatedCount != 1) throw PlayerNotFoundException(playerId)
    }

    override fun addMoney(playerId: Int, value: Long) = transaction {
        // Not negative check so we can go to negative
        val updatedCount = Players
            .update({ Players.userId eq playerId }, 1) {
                with (SqlExpressionBuilder) {
                    it[Players.money] = Players.money + value
                }
            }

        if (updatedCount != 1) throw PlayerNotFoundException(playerId)
    }

    override fun getGuildId(playerId: Int): Int? = transaction {
        (Players
            .slice(Players.guildId)
            .select { Players.userId eq playerId }
            .firstOrNull() ?: throw PlayerNotFoundException(playerId)).let {
                it[Players.guildId]
            }
            ?.value
    }

    override fun setGuildId(playerId: Int, guildId: Int?) = transaction {
        val gId = if (guildId == null) null else (Guilds
            .slice(Guilds.id)
            .select { Guilds.id eq guildId }
            .firstOrNull()
            ?.let { it[Guilds.id] } ?: throw GuildNotFoundException(guildId))


        val updatedCount = Players
            .update({ Players.userId eq playerId }, 1) {
                it[Players.guildId] = gId
            }

        if (updatedCount != 1) throw PlayerNotFoundException(playerId)
    }

    override fun getExperience(playerId: Int): Long = transaction {
        (Players
            .slice(Players.experience)
            .select { Players.userId eq playerId }
            .firstOrNull() ?: throw PlayerNotFoundException(playerId)).let {
            it[Players.experience]
        }
    }

    override fun setExperience(playerId: Int, value: Long) = transaction {
        val updatedCount = Players
            .update({ Players.userId eq playerId }, 1) {
                it[Players.experience] = value
            }

        if (updatedCount != 1) throw PlayerNotFoundException(playerId)
    }

    override fun addExperience(playerId: Int, value: Long) = transaction {
        val updatedCount = Players
            .update({ Players.userId eq playerId }, 1) {
                with (SqlExpressionBuilder) {
                    it[Players.experience] = Players.experience + value
                }
            }

        if (updatedCount != 1) throw PlayerNotFoundException(playerId)
    }
}