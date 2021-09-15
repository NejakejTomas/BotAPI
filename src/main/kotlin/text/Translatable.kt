package text

class Translatable(content: String, vararg args: Text) : TextWithArguments("Translatable", content, *args)