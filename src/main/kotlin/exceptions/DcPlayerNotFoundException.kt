package exceptions

import text.Literal
import text.Translatable

class DcPlayerNotFoundException(dcSnowflake: ULong) : NotFoundException(Translatable("PlayerWithId%sWasNotFound", Literal(dcSnowflake.toString())))