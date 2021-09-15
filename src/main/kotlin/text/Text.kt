package text

abstract class Text(val name: String, val content: String) {
    abstract override fun toString(): String
}