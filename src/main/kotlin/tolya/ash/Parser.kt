package tolya.ash

class Parser : TokenVisitor<List<Token>> {
    private val stack = mutableListOf<OpToken>()

    var isCorrect: Boolean = true
        private set

    override fun visitPar(t: ParToken): List<Token> = if (t.isLeft) {
        stack += t
        emptyList()
    } else {
        val result = mutableListOf<Token>()
        while (stack.isNotEmpty() && stack.last().type != TokenType.LPAR) {
            result += stack.removeLast()
        }
        if (stack.isEmpty()) {
            result += ErrorToken(t.str, "Expected ( for")
            isCorrect = false
        } else {
            stack.removeLast()
        }
        result
    }


    override fun visitOp(t: OpToken): List<Token> {
        val result = mutableListOf<Token>()
        while (stack.isNotEmpty() && stack.last().level >= t.level) {
            result += stack.removeLast()
        }
        stack += t
        return result
    }

    override fun visitNumber(t: NumberToken): List<Token> = listOf(t)

    override fun visitError(t: ErrorToken): List<Token> = listOf(t).also {
        isCorrect = false
    }

    override fun visitEOF(): List<Token> {
        val result = stack.reversed<Token>().toMutableList()
        result += EOFToken
        stack.clear()
        return result
    }
}

fun parse(seq: Sequence<Token>): Sequence<Token> {
    val parser = Parser()
    return seq
        .map {
            if (!parser.isCorrect) {
                null
            } else {
                it.visit(parser)
            }
        }
        .flatMap { it ?: emptyList() }
}
