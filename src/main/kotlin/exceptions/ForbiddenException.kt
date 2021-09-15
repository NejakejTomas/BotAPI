package exceptions

import io.ktor.http.*
import text.Text
import text.Translatable

open class ForbiddenException(message: Text = Translatable("ForbiddenOperation")) :
    HttpException(message, HttpStatusCode.Forbidden)