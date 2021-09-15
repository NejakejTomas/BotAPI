package exceptions

import text.Literal
import text.Translatable

class PlayerNotFoundException(playerId: Int) : NotFoundException(Translatable("PlayerWithId%sWasNotFound", Literal(playerId.toString())))