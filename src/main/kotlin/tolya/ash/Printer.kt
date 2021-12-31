package tolya.ash

class Printer : TokenVisitor<String> {
    override fun visitPar(t: ParToken): String = " ${t.str}"

    override fun visitOp(t: OpToken): String = " ${t.str}"

    override fun visitNumber(t: NumberToken): String = " ${t.str}"

    override fun visitError(t: ErrorToken): String =
        "\nError happened at ${t.str}.\nMessage: ${t.message}"

    override fun visitEOF(): String = "\n"
}
