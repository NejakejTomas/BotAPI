package exceptions

import text.Literal
import text.Translatable

class ItemDoesNotExist(itemId: Int) : NotFoundException(Translatable("ItemWithId%sDoesNotExist", Literal(itemId.toString())))