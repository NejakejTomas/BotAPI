package text

abstract class TextWithArguments(name: String, content: String, vararg val args: Text) : Text(name, content) {
    override fun toString(): String = if (args.isEmpty()) "<$name value=\"$content\" />"
    else "<$name value=\"$content\">${args.joinToString("")}</$name>"
}