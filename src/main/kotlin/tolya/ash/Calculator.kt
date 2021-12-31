package tolya.ash

class Calculator: TokenVisitor<Unit> {
    val stack = mutableListOf<Int>()

    override fun visitPar(t: ParToken) {
        throw IllegalArgumentException("Unexpected token ${t.str}")
    }

    override fun visitOp(t: OpToken) {
        require(stack.size >= 2) { "Not enough arguments for operator ${t.str}" }
        val second = stack.removeLast()
        val first = stack.removeLast()
        val result = OP_TO_FUNCTION[t.type]!!(first, second)
        stack += result
    }

    override fun visitNumber(t: NumberToken) {
        stack += t.asInt
    }

    override fun visitError(t: ErrorToken) {
        throw RuntimeException("Cannot evaluate invalid expression")
    }

    fun getResult(): Int {
        return stack.last()
    }

    override fun visitEOF() {
        require(stack.size == 1) { "Expected operator" }
    }
}

fun calc(seq: Sequence<Token>): Int {
    val calculator = Calculator()
    seq.forEach { it.visit(calculator) }
    return calculator.getResult()
}
