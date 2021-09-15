import dbs.tables.*
import dbs.tables.*
import dbs.tables.Translations.translation
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun main() {
//    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
//
//    transaction {
//
//        addLogger(StdOutSqlLogger)
//
//        SchemaUtils.create(Guilds)
//        SchemaUtils.create(Inventories)
//        SchemaUtils.create(Items)
//        SchemaUtils.create(Players)
//        SchemaUtils.create(Translations)
//        SchemaUtils.create(Users)
//
//        val inventoryObj = Inventory.new {
//            size = 20
//        }
//
//        val item1Obj = Item.new {
//            inventory = inventoryObj
//            name = "item1Name"
//            description = "item1Description"
//        }
//
//        val item2Obj = Item.new {
//            inventory = inventoryObj
//            name = "item2Name"
//            description = "item2Description"
//        }
//
//        val guildObj = Guild.new {
//            dcSnowflake = 869556260423467049U
//            name = "guildName"
//            description = "guildDescription"
//        }
//
//        val userEntityObj = UserEntity.new {
//            dcSnowflake = 155951502387707904U
//            dateJoined = LocalDateTime.now()
//            isAdmin = false
//        }
//
//        val playerEntityObj = PlayerEntity.new {
//            user = userEntityObj
//            inventory = inventoryObj
//            guild = guildObj
//            money = 42
//            experience = 69
//        }
//
//        (playerEntityObj.guild.players as Iterable<PlayerEntity>).forEach {
//            println(it.user.dcSnowflake)
//        }
//
//        Translations.insert {
//            it[translationString] = "schoolOfCat"
//            it[languageCode] = "cs-cz"
//            it[translation] = "Škola Kočky"
//        }
//
//        Translations.insert {
//            it[translationString] = "schoolOfCat"
//            it[languageCode] = "en-us"
//            it[translation] = "School of the Cat"
//        }
//
//        var item = Translations.select {
//            (Translations.languageCode eq "cs-cz") and (Translations.translationString eq "schoolOfCat")
//        }.first()[translation]
//        val i = 5
//    }
}