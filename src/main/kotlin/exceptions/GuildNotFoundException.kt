package exceptions

import text.Literal
import text.Translatable

class GuildNotFoundException(guildId: Int) : NotFoundException(Translatable("GuildWithId%sWasNotFound", Literal(guildId.toString())))