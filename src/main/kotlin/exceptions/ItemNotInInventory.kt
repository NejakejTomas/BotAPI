package exceptions

import text.Literal
import text.Translatable

class ItemNotInInventory(itemId: Int, playerId: Int)
    : NotFoundException(Translatable("ItemWithId%sWasNotFoundInInventoryOfPlayerWithId%s", Literal(itemId.toString()), Literal(playerId.toString()))) {
}