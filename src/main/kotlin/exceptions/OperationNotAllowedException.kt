package exceptions

import text.Text
import text.Translatable

class OperationNotAllowedException(operation: Text, reason: Text? = null) :
    ForbiddenException(
        if (reason == null) Translatable("Operation%sIsNotAllowed", operation)
        else Translatable("Operation%sIsNotAllowedBecause%s", operation, reason)
    )