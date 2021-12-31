package tolya.ash

enum class TokenType {
    LPAR,
    RPAR,
    PLUS,
    MINUS,
    MUL,
    DIV,
    NUMBER,
    ERROR,
    EOF
}

val CHAR_TO_OP = mapOf(
    '(' to TokenType.LPAR,
    ')' to TokenType.RPAR,
    '+' to TokenType.PLUS,
    '-' to TokenType.MINUS,
    '*' to TokenType.MUL,
    '/' to TokenType.DIV
)

val OP_TO_LEVEL = mapOf(
    TokenType.LPAR to -1,
    TokenType.RPAR to -1,
    TokenType.PLUS to 0,
    TokenType.MINUS to 0,
    TokenType.MUL to 1,
    TokenType.DIV to 1
)

val OP_TO_FUNCTION = mapOf<TokenType, (Int, Int) -> Int>(
    TokenType.PLUS to Int::plus,
    TokenType.MINUS to Int::minus,
    TokenType.MUL to Int::times,
    TokenType.DIV to Int::div
)

interface TokenVisitor<T> {
    fun visitPar(t: ParToken): T
    fun visitOp(t: OpToken): T
    fun visitNumber(t: NumberToken): T
    fun visitError(t: ErrorToken): T
    fun visitEOF(): T
}

abstract class Token(
    val type: TokenType, val str: String
) {
    abstract fun <T> visit(tv: TokenVisitor<T>): T
}

open class OpToken(
    type: TokenType, str: String
) : Token(type, str) {
    val level = OP_TO_LEVEL[type]!!

    override fun <T> visit(tv: TokenVisitor<T>) = tv.visitOp(this)
}

class ParToken(
    val isLeft: Boolean, str: String
) : OpToken(if (isLeft) TokenType.LPAR else TokenType.RPAR, str) {
    override fun <T> visit(tv: TokenVisitor<T>) = tv.visitPar(this)
}


class NumberToken(
    str: String
) : Token(TokenType.NUMBER, str) {
    val asInt: Int
        get() = str.toInt()

    override fun <T> visit(tv: TokenVisitor<T>) = tv.visitNumber(this)
}

class ErrorToken(
    str: String, val message: String
) : Token(TokenType.ERROR, str) {
    override fun <T> visit(tv: TokenVisitor<T>) = tv.visitError(this)
}

object EOFToken : Token(TokenType.EOF, "#eof") {
    override fun <T> visit(tv: TokenVisitor<T>): T = tv.visitEOF()
}

interface State {
    fun acceptEOF(state: StateHolder): Token?
    fun acceptChar(state: StateHolder, c: Char): Pair<Boolean, Token>?
}

class StateHolder(var state: State)

object StartState : State {
    override fun acceptEOF(state: StateHolder): Token? {
        state.state = EndState
        return EOFToken
    }

    override fun acceptChar(state: StateHolder, c: Char): Pair<Boolean, Token>? {
        if (c.isWhitespace()) return null
        CHAR_TO_OP[c]?.let { op ->
            return true to if (op == TokenType.LPAR || op == TokenType.RPAR) {
                ParToken(op == TokenType.LPAR, c.toString())
            } else {
                OpToken(op, c.toString())
            }
        }
        if (c.isDigit()) {
            state.state = NumberState(c)
            return null
        }
        state.state = EndState
        return false to ErrorToken(c.toString(), "Unexpected char $c")
    }
}

class NumberState(c: Char) : State {
    private val chars = mutableListOf(c)

    override fun acceptEOF(state: StateHolder): Token? {
        state.state = StartState
        return NumberToken(chars.joinToString(""))
    }

    override fun acceptChar(state: StateHolder, c: Char): Pair<Boolean, Token>? {
        if (c.isDigit()) {
            chars += c
            return null
        } else {
            state.state = StartState
            return false to NumberToken(chars.joinToString(""))
        }
    }
}

object EndState : State {
    override fun acceptEOF(state: StateHolder): Token? =
        throw IllegalArgumentException("End state doesn't accept anything")

    override fun acceptChar(state: StateHolder, c: Char): Pair<Boolean, Token>? =
        throw IllegalArgumentException("End state doesn't accept anything")
}


fun tokenizeString(s: CharSequence): Sequence<Token> {
    if (s.isEmpty()) return emptySequence()
    val it = s.iterator()
    var lastC: Char? = null
    val stateHolder = StateHolder(StartState)

    return generateSequence {
        var result: Pair<Boolean, Token?>?
        do {
            if (stateHolder.state is EndState) {
                return@generateSequence null
            }
            result = if (lastC == null && !it.hasNext()) {
                false to stateHolder.state.acceptEOF(stateHolder)
            } else {
                if (lastC == null) lastC = it.nextChar()
                stateHolder.state.acceptChar(stateHolder, lastC!!)
            }
            if (result == null || result.first) {
                lastC = null
            }
        } while (result == null)
        return@generateSequence result.second
    }
}
