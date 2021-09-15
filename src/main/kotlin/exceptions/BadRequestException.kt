package exceptions

import io.ktor.http.*
import text.Text
import text.Literal

class BadRequestException(message: Text = Literal("")) : HttpException(message, HttpStatusCode.BadRequest)