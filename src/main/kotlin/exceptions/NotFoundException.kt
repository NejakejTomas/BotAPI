package exceptions

import io.ktor.http.*
import text.Text
import text.Translatable

open class NotFoundException(message: Text = Translatable("ResourceNotFound")) : HttpException(message, HttpStatusCode.NotFound)