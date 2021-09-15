package text

class Literal(content: String, vararg args: Text) : TextWithArguments("Text", content, *args)