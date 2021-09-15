package exceptions

import io.ktor.http.*
import text.Text

abstract class HttpException private constructor(override val message: String, val httpStatusCode: HttpStatusCode) : Exception(message) {
    constructor(msg: Text, httpStatusCode: HttpStatusCode) : this(msg.toString(), httpStatusCode)
}